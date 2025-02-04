function createCombinedVideo()
% createCombinedVideo - Combina un video generado por Brainstorm con frames dinámicos
% generados por MATLAB para crear un video final.
%
% La función realiza lo siguiente:
%  1. Solicita al usuario la ruta del video de Brainstorm mediante un cuadro
%     de diálogo (uigetfile).
%  2. Solicita al usuario la carpeta que contiene los frames dinámicos de MATLAB
%     mediante un cuadro de diálogo (uigetdir).
%  3. Solicita el nombre (y ruta) del video final de salida mediante un cuadro
%     de diálogo (inputdlg).
%  4. Verifica que la carpeta de frames exista.
%  5. Si el video de Brainstorm está en formato AVI, lo convierte (usando ffmpeg)
%     a un nuevo video con dimensiones ajustadas (múltiplos de 2).
%  6. Lee el video convertido y, para cada frame, combina el frame de Brainstorm
%     con el frame correspondiente de MATLAB (si existe) usando la función
%     combineFrames.
%  7. Escribe cada frame combinado en un nuevo video MPEG-4.
%  8. Al finalizar, cierra el escritor de video y muestra un mensaje de éxito.
%
% Requisitos:
%   - FFmpeg debe estar instalado y accesible desde la línea de comandos.
%   - Los frames dinámicos de MATLAB deben estar nombrados en el formato:
%       frame_00001.png, frame_00002.png, etc.
%
% Ejemplo de uso:
%   >> createCombinedVideo();

    %% 1. Solicitar al usuario la ruta del video de Brainstorm (.avi)
    [brainstorm_filename, brainstorm_filepath] = uigetfile({'*.avi','Archivos AVI (*.avi)'}, ...
        'Seleccione el video generado por Brainstorm');
    if isequal(brainstorm_filename, 0)
        error('No se seleccionó el video de Brainstorm.');
    end
    % Se construye la ruta completa del video de Brainstorm.
    brainstorm_video = fullfile(brainstorm_filepath, brainstorm_filename);
    
    %% 2. Solicitar al usuario la carpeta que contiene los frames dinámicos de MATLAB
    folder_freqs = uigetdir('', 'Seleccione la carpeta que contiene los frames dinámicos de MATLAB');
    if isequal(folder_freqs, 0)
        error('No se seleccionó la carpeta de frames.');
    end
    
    %% 3. Solicitar al usuario el nombre del video final de salida
    output_video_input = inputdlg('Ingrese el nombre del video de salida (ej. 10a40.mp4):', ...
                                  'Nombre del Video de Salida', [1 50]);
    if isempty(output_video_input)
        error('No se proporcionó el nombre del video de salida.');
    end
    % Se elimina cualquier espacio en blanco extra.
    output_video = strtrim(output_video_input{1});
    
    %% 4. Verificar que la carpeta de frames existe
    if ~exist(folder_freqs, 'dir')
        error('No se encontró la carpeta de frames dinámicos de MATLAB. Genere los frames primero.');
    end
    
    %% 5. Convertir el video de Brainstorm si es necesario
    % Se definirá un video convertido (en este ejemplo, se usará '16fps.avi')
    converted_video = fullfile(fileparts(brainstorm_video), brainstorm_filename);
    if ~isfile(converted_video)
        % Se utiliza FFmpeg para convertir el video. La opción "-vf" con el filtro
        % "scale=trunc(iw/2)*2:trunc(ih/2)*2" se asegura de que el ancho y alto sean
        % múltiplos de 2, requisito para algunos códecs.
        ffmpeg_command = sprintf('ffmpeg -i "%s" -vf "scale=trunc(iw/2)*2:trunc(ih/2)*2" -c:v libx264 -crf 23 -preset fast "%s"', ...
            brainstorm_video, converted_video);
        system(ffmpeg_command);
    end
    
    %% 6. Crear objetos para leer y escribir video
    % Se crea un objeto VideoReader para el video convertido.
    vReader = VideoReader(converted_video);
    
    % Se crea un objeto VideoWriter para el video final, utilizando el códec MPEG-4.
    vWriter = VideoWriter(output_video, 'MPEG-4');
    vWriter.FrameRate = 8; % 8 FPS: cada frame dura 0.125 segundos
    open(vWriter);
    
    %% 7. Determinar las dimensiones del primer frame combinado
    % Se lee el primer frame del video de Brainstorm.
    first_frame = readFrame(vReader);
    % Se construye la ruta del primer frame dinámico generado por MATLAB.
    first_matlab_frame_path = fullfile(folder_freqs, sprintf('frame_%05d.png', 1));
    if exist(first_matlab_frame_path, 'file')
        first_matlab_frame = imread(first_matlab_frame_path);
        % Se combina el primer frame del video de Brainstorm con el primer frame de MATLAB.
        first_combined_frame = combineFrames(first_frame, first_matlab_frame);
    else
        first_combined_frame = first_frame;
    end
    
    % Se extraen las dimensiones (altura y ancho) del frame combinado.
    frame_height = size(first_combined_frame, 1);
    frame_width  = size(first_combined_frame, 2);
    
    %% 8. Rebobinar el lector de video para comenzar desde el inicio
    vReader.CurrentTime = 0;
    
    %% 9. Procesar y combinar todos los frames
    i = 1; % Contador de frames
    while hasFrame(vReader)
        % Leer un frame del video de Brainstorm
        brain_frame = readFrame(vReader);
        
        % Construir la ruta del frame correspondiente de MATLAB
        matlab_frame_path = fullfile(folder_freqs, sprintf('frame_%05d.png', i));
        
        if exist(matlab_frame_path, 'file')
            matlab_frame = imread(matlab_frame_path);
            % Combinar el frame de Brainstorm y el frame de MATLAB
            combined_frame = combineFrames(brain_frame, matlab_frame);
        else
            % Si no existe el frame de MATLAB, se utiliza solo el frame de Brainstorm
            combined_frame = brain_frame;
        end
        
        % Redimensionar el frame combinado para que tenga las dimensiones deseadas
        combined_frame = imresize(combined_frame, [frame_height, frame_width]);
        
        % Escribir el frame combinado en el video final
        writeVideo(vWriter, combined_frame);
        
        % Incrementar el contador
        i = i + 1;
    end
    
    %% 10. Cerrar el objeto VideoWriter para finalizar el archivo de video
    close(vWriter);
    
    % Mostrar mensaje de éxito
    disp('El video combinado se ha generado con éxito.');
    
    %% Función anidada: combineFrames
    % Esta función combina un frame del video de Brainstorm con un frame de MATLAB.
    % El frame de MATLAB se redimensiona y se sobrepone en la esquina inferior derecha
    % del frame de Brainstorm.
    function combined = combineFrames(brain_frame, matlab_frame)
        % Obtener las dimensiones del frame de Brainstorm
        target_height = size(brain_frame, 1);
        target_width  = size(brain_frame, 2);
        
        % Definir el factor de escala para reducir el tamaño del frame de MATLAB
        scale_factor = 0.5; % Se reduce al 50% del tamaño original
        % Calcular la nueva altura y ancho del frame de MATLAB
        matlab_height = round(target_height * scale_factor) - 16; % Se resta un ajuste adicional
        matlab_width  = round(target_width  * scale_factor);
        
        % Redimensionar el frame de MATLAB a las dimensiones calculadas
        matlab_frame_resized = imresize(matlab_frame, [matlab_height, matlab_width]);
        
        % Crear una copia del frame de Brainstorm para sobreponer el frame redimensionado
        combined = brain_frame;
        
        % Calcular la posición para insertar el frame de MATLAB:
        % Se posiciona en la esquina inferior derecha.
        start_row = target_height - matlab_height + 1;
        start_col = target_width  - matlab_width  + 1;
        
        % Verificar que las coordenadas de inicio sean válidas
        if start_row < 1
            start_row = 1;
        end
        if start_col < 1
            start_col = 1;
        end
        
        % Insertar el frame de MATLAB redimensionado en la posición calculada
        combined(start_row:start_row + matlab_height - 1, start_col:start_col + matlab_width - 1, :) = matlab_frame_resized;
        
        % Asegurarse de que las dimensiones del frame combinado sean múltiplos de 2
        h = size(combined, 1);
        w = size(combined, 2);
        if mod(h, 2) ~= 0
            combined = combined(1:end-1, :, :);
        end
        if mod(w, 2) ~= 0
            combined = combined(:, 1:end-1, :);
        end
    end

end

