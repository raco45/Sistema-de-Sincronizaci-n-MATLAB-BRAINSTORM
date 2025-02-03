function images_fft_powers()
% images_fft_powers - 
%   Genera imágenes dinámicas de gráficas de "powers" y de la FFT a lo largo del tiempo,
%   usando datos leídos desde archivos CSV, para un intervalo de tiempo especificado.
%
% Sintaxis:
%   images_fft_powers2(inicio, fin, freq_muestreo)
%
% Entradas:
%   inicio        - Tiempo de inicio (en segundos) del intervalo a procesar.
%   fin           - Tiempo de fin (en segundos) del intervalo a procesar.
%   freq_muestreo - Frecuencia de muestreo (valor numérico) que se usará para el cálculo de
%                   la FFT (se utiliza en el cálculo del parámetro Fs).
%
% Descripción:
%   Esta función solicita al usuario, mediante ventanas emergentes, los siguientes
%   parámetros:
%     - "inicio": tiempo de inicio (en segundos) del intervalo a procesar.
%     - "fin": tiempo de fin (en segundos) del intervalo a procesar.
%     - "freq_muestreo": frecuencia de muestreo que se usará para el cálculo de la FFT.
%
%   Luego, la función solicita mediante diálogos la carpeta de salida para guardar imágenes,
%   y los archivos CSV de "powers" y "signals". Se realiza un preprocesamiento de los datos,
%   se ajusta el intervalo de tiempo y se calcula la FFT para cada ventana de tiempo (0.5 s),
%   generando imágenes que se exportan en la carpeta seleccionada.
%
% Notas:
%   - Se fuerza el renderizado de figuras con 'painters' para evitar problemas
%     relacionados con la aceleración gráfica.
%   - Si la función se ejecuta sin los parámetros mínimos requeridos, se genera un error.

    %----------------------------------------------------------------------
    % Configuración inicial y renderizado
    %----------------------------------------------------------------------
    % Se desactiva la renderización en OpenGL (comentada) y se fuerza el uso
    % del renderizador 'painters' para todas las figuras (esto ayuda a que la
    % salida gráfica sea independiente de los drivers gráficos del equipo).
    warning('off', 'all');  % (Esta línea está comentada; se podría usar para desactivar avisos)
    set(0, 'DefaultFigureRenderer', 'painters');
    %opengl('save', 'software');  % (Línea comentada, ya que se prefiere el renderizador 'painters')

    %% Solicitar parámetros de entrada al usuario mediante ventana emergente
    prompt = {'Ingrese el tiempo de inicio (en segundos):', ...
              'Ingrese el tiempo de fin (en segundos):', ...
              'Ingrese la frecuencia de muestreo:'};
    dlgtitle = 'Parámetros de Entrada';
    dims = [1 50];
    definput = {'0.125','10','90'};  % Valores por defecto (se pueden ajustar)
    answer = inputdlg(prompt, dlgtitle, dims, definput);
    
    % Si el usuario cancela, se termina la función
    if isempty(answer)
        disp('Operación cancelada por el usuario.');
        return;
    end
    
    % Convertir las respuestas a números
    inicio = str2double(answer{1});
    fin = str2double(answer{2});
    freq_muestreo = str2double(answer{3});

    %----------------------------------------------------------------------
    % Validación de parámetros de entrada
    %----------------------------------------------------------------------
    
    if inicio >= fin
        error('El parámetro "inicio" debe ser menor que "fin".');
    end
    if (fin - inicio) < 1
        error('El intervalo (fin - inicio) debe ser al menos 1 segundo.');
    end

    %----------------------------------------------------------------------
    % Seleccionar carpeta de salida para guardar imágenes
    %----------------------------------------------------------------------
    % Se abre una ventana de diálogo para que el usuario seleccione una carpeta
    % donde se guardarán las imágenes generadas de las gráficas.
    freqs_folder = uigetdir('Documentos', 'Selecciona una carpeta para almacenar las imágenes de las gráficas de powers y FFT');
    if freqs_folder == 0
        error('No se seleccionó ninguna carpeta.');     
    else
        fprintf('Carpeta seleccionada: %s\n', freqs_folder);
    end

    %----------------------------------------------------------------------
    % Seleccionar y validar los archivos CSV
    %----------------------------------------------------------------------
    % Se llama a la función validate_file_powers para solicitar y validar el
    % archivo CSV de "powers". Si la validación falla, se detiene la ejecución.
    [filename_powers, filepath_powers] = validate_file_powers();
    if strcmp(filename_powers, '0')
        error('NO SE PUEDE CONTINUAR');
    end

    % Se realiza lo mismo para el archivo CSV de "signals".
    [filename_signals, filepath_signals] = validate_file_signals();
    if strcmp(filename_signals, '0')
        error('NO SE PUEDE CONTINUAR');
    end

    %----------------------------------------------------------------------
    % Lectura de los datos de los archivos CSV
    %----------------------------------------------------------------------
    % Se leen los archivos CSV usando readtable y se construye la ruta completa
    % a partir del nombre y la carpeta.
    data_powers = readtable(fullfile(filepath_powers, filename_powers));
    data_signals = readtable(fullfile(filepath_signals, filename_signals));

    %----------------------------------------------------------------------
    % Validación y ajuste del intervalo de tiempo según data_powers
    %----------------------------------------------------------------------
    % Se asume que la columna 'Time' en data_powers está en segundos.
    all_times_powers = data_powers.Time;
    max_time_power = max(all_times_powers);
    % Se verifica que el intervalo [inicio, fin] esté dentro de los límites
    % (desde 0.125 s hasta el máximo tiempo registrado en powers).
    if inicio < 0.125 || fin > max_time_power
        error('Los valores de inicio y fin deben estar entre 0.125 y %f', max_time_power);
    end

    % Debido a que el archivo powers varía en intervalos de 0.125 s,
    % se redondea hacia abajo el valor de "inicio" y se redondea hacia arriba "fin".
    adjusted_inicio = floor(inicio/0.125) * 0.125;
    adjusted_fin    = ceil(fin/0.125) * 0.125;
    fprintf('Intervalo ajustado: inicio = %.3f s, fin = %.3f s\n', adjusted_inicio, adjusted_fin);

    % Se filtran los datos de powers para conservar únicamente los registros
    % que se encuentran dentro del intervalo ajustado.
    idx_powers = (all_times_powers >= adjusted_inicio) & (all_times_powers <= adjusted_fin);
    data_powers = data_powers(idx_powers, :);

    %----------------------------------------------------------------------
    % Filtrar los datos de signals para el mismo intervalo
    %----------------------------------------------------------------------
    % Se extrae la columna de tiempo de data_signals y se redondea a 3 decimales.
    time2 = round(data_signals.Time, 3);
    % Se crea un vector lógico que indica qué registros están dentro del intervalo.
    idx_signals = (time2 >= adjusted_inicio) & (time2 <= adjusted_fin);
    % Se filtra la variable time2 y también la matriz de señales.
    time2 = time2(idx_signals);
    signals = data_signals{idx_signals, 2:15};  % Se asume que las columnas 2 a 15 contienen los datos de los electrodos.
    electrode_names = data_signals.Properties.VariableNames(2:15);  % Nombres de los electrodos.

    %----------------------------------------------------------------------
    % Definición de parámetros para actualización y ventana FFT
    %----------------------------------------------------------------------
    intervalos_potencias = 0.125;   % Cada 0.125 s se actualiza la gráfica de potencias.
    intervalos_fft = 0.5;           % Se usa una ventana de 0.5 s para calcular la FFT.

    % Cálculo de la frecuencia de muestreo "Fs".
    % Se define Fs como 2*(freq_muestreo+2). (Esta fórmula depende del contexto
    % y de cómo se definan los parámetros en el experimento).
    Fs = 2*(freq_muestreo+2);

    % Se calcula el tamaño de la ventana en número de muestras.
    window_size = round(intervalos_fft * Fs);
    % Se fuerza que window_size sea par (si es impar, se le suma 1).
    if mod(window_size, 2) ~= 0
        window_size = window_size + 1;
    end

    % step_size es el número de muestras correspondientes a 0.125 s, aunque
    % aquí se utiliza solo como referencia.
    step_size = round(intervalos_potencias * Fs);

    %----------------------------------------------------------------------
    % Preparar el vector de frecuencias para la FFT
    %----------------------------------------------------------------------
    % Se genera un vector de frecuencias a partir de 0 hasta (window_size-1),
    % escalado por Fs/window_size.
    frequencies2 = (0:window_size-1) * (Fs/window_size);
    % Se toma solo la mitad de los valores, ya que la FFT es simétrica.
    half_range = 1:floor(window_size/2);
    frequencies2 = frequencies2(half_range);

    %----------------------------------------------------------------------
    % Definir bandas para la gráfica de potencias y sus colores asociados
    %----------------------------------------------------------------------
    frequencies = {'Theta', 'Alpha', 'BetaL', 'BetaH', 'Gamma'};
    colors = {'blue', 'lightgreen', 'red', 'orange', 'yellow'};

    %----------------------------------------------------------------------
    % Precomputar la FFT para cada frame de powers (dentro del intervalo seleccionado)
    %----------------------------------------------------------------------
    total_frames_potencias = height(data_powers);  
    % Se crea una celda para almacenar la FFT de cada canal y para cada frame.
    fft_data = cell(total_frames_potencias, size(signals, 2));

    % Se calcula la relación entre la cantidad de muestras en signals y el número
    % de frames en data_powers (aproximadamente cuántas muestras corresponden a cada frame).
    ratio_muestras = round(length(time2) / total_frames_potencias);

    % Bucle para procesar cada frame de powers y calcular la FFT correspondiente.
    for frame = 1:total_frames_potencias
        % Determinar el índice central en el vector de señales correspondiente al frame actual.
        center_idx = min(1 + (frame - 1) * ratio_muestras, length(time2));

        % Calcular la mitad de la ventana en muestras.
        half_window = floor(window_size / 2);
        % Calcular el índice de inicio de la ventana; se asegura que no sea menor que 1.
        start_idx = max(1, center_idx - half_window);
        % El índice final se calcula sumando el tamaño de la ventana - 1.
        end_idx = start_idx + window_size - 1;
        % Si el índice final excede la longitud de time2, se ajusta para que encaje.
        if end_idx > length(time2)
            end_idx = length(time2);
            start_idx = end_idx - window_size + 1;
        end

        % Se extrae la ventana de señales correspondiente a este frame.
        window_signals = signals(start_idx:end_idx, :);

        % Para cada canal (electrodo), se calcula la FFT y se almacena la magnitud
        % en escala logarítmica (se suma eps para evitar logaritmo de cero).
        for i = 1:size(window_signals, 2)
            fft_signal = fft(window_signals(:, i));
            magnitude = abs(fft_signal / length(window_signals(:, i)));  % Normalización por el número de muestras
            valid_range = 1:min(length(magnitude), length(half_range));  % Asegurarse de no exceder los límites
            fft_data{frame, i} = log10(magnitude(valid_range) + eps);
        end
    end

    %----------------------------------------------------------------------
    % Configuración de la figura y sus subplots
    %----------------------------------------------------------------------
    % Se crea la figura principal para mostrar las gráficas.
    hFig = figure('Name', 'FFT y potencias', 'NumberTitle', 'off', ...
                  'MenuBar', 'none', 'ToolBar', 'none', ...
                  'Renderer', 'painters', ...      % Se fuerza el renderizador 'painters'
                  'DockControls', 'off');

    % Se crean dos subplots dentro de la figura:
    % hAxes1 para la gráfica de barras de potencias y hAxes2 para la FFT.
    hAxes1 = subplot(2,1,1);
    hAxes2 = subplot(2,1,2);

    %----------------------------------------------------------------------
    % Configuración de la gráfica de barras para las potencias
    %----------------------------------------------------------------------
    % Se crea una gráfica de barras en hAxes1, inicialmente con valores cero,
    % con el modo 'flat' para asignar colores individualmente.
    b = bar(hAxes1, zeros(1, length(frequencies)), 'FaceColor', 'flat');
    % Se asigna a cada barra un color específico según la lista 'colors'.
    for i = 1:length(frequencies)
        b.CData(i, :) = rgb(colors{i});
    end

    % Se verifica que la matriz de señales no esté vacía.
    if isempty(signals)
        error('Error: No se encontraron datos de signals válidos en el intervalo especificado.');
    end

    %----------------------------------------------------------------------
    % Configuración de la gráfica de la FFT
    %----------------------------------------------------------------------
    % Se crea un objeto de tipo gráfico (line) para cada canal de signals,
    % inicialmente con valores cero, y se asigna un nombre de display (etiqueta)
    % usando el nombre del electrodo.
    lines = gobjects(1, size(signals, 2));
    for i = 1:size(signals, 2)
        lines(i) = plot(hAxes2, frequencies2, zeros(size(frequencies2)), 'DisplayName', electrode_names{i});
        hold(hAxes2, 'on');  % Se mantiene el gráfico para poder superponer múltiples líneas
    end
    hold(hAxes2, 'off');     % Se desactiva el hold

    %----------------------------------------------------------------------
    % Configuración de etiquetas y títulos de los ejes
    %----------------------------------------------------------------------
    xlabel(hAxes1, 'Bandas de Frecuencia');
    ylabel(hAxes1, 'Power (µV²)');
    title(hAxes1, 'Potencias en el tiempo');

    xlabel(hAxes2, 'Frecuencia (Hz)');
    % Se obtienen los límites actuales del eje x para configurar las marcas.
    xLimits = xlim();            
    xticks(xLimits(1):4:xLimits(2));  % Se configuran marcas cada 4 unidades en x.
    ylabel(hAxes2, 'log(Voltios) (μV)');
    title(hAxes2, 'Transformada de Fourier en el tiempo');
    % Se agrega una leyenda a la gráfica de la FFT, ubicada en la esquina noreste.
    legend(hAxes2, 'show', 'Location', 'northeastoutside', 'FontSize', 0.65 * get(gca, 'FontSize'));

    %----------------------------------------------------------------------
    % Creación del botón Play/Pause/Reiniciar
    %----------------------------------------------------------------------
    % Se define una variable booleana que indica si la animación se está ejecutando.
    isPlaying = false;  % Comienza en pausa
    setappdata(hFig, 'isPlaying', isPlaying);  % Se almacena en el 'appdata' de la figura
    % Se crea un botón tipo 'togglebutton' que controla la reproducción de la animación.
    playButton = uicontrol('Style', 'togglebutton', 'String', 'Play', ...
                           'Position', [10 10 60 30], ...  % Posición y tamaño del botón (en píxeles)
                           'Callback', @playPauseCallback); % Función que se ejecuta al hacer clic

    %----------------------------------------------------------------------
    % Inicialización del contador de frames y del Timer
    %----------------------------------------------------------------------
    frame = 1;  % Inicialización del contador de frames
    % Se crea un objeto Timer para actualizar las gráficas en intervalos fijos
    % (cada 0.125 s, de acuerdo a intervalos_potencias).
    t = timer('ExecutionMode', 'fixedRate', 'Period', intervalos_potencias, ...
              'TimerFcn', @updateGraphics, 'StartDelay', Inf);

    %----------------------------------------------------------------------
    % Función callback para el botón Play/Pause/Reiniciar
    %----------------------------------------------------------------------
    function playPauseCallback(src, ~)
        % Se obtiene el estado actual de reproducción almacenado en la figura.
        isPlaying = getappdata(hFig, 'isPlaying');
        if isPlaying
            % Si se está reproduciendo, se detiene el Timer y se cambia el texto del botón a 'Play'.
            stop(t);
            set(src, 'String', 'Play');
            disp('Animación pausada.');
        else
            if frame <= total_frames_potencias
                % Si aún quedan frames por reproducir, se inicia el Timer y se cambia el botón a 'Pause'.
                start(t);
                set(src, 'String', 'Pause');
                disp('Animación reanudada.');
            else
                % Si ya se han reproducido todos los frames, se reinicia el contador y se inicia nuevamente.
                frame = 1;
                set(src, 'String', 'Pause');
                start(t);
                disp('Animación reiniciada.');
            end
        end
        % Se actualiza el valor de 'isPlaying' en el appdata de la figura.
        setappdata(hFig, 'isPlaying', ~isPlaying);
    end

    %----------------------------------------------------------------------
    % Función callback para actualizar las gráficas en cada frame (TimerFcn)
    %----------------------------------------------------------------------
    function updateGraphics(~, ~)
        % Si ya se han procesado todos los frames, se detiene el Timer y se
        % cambia el botón a 'Reiniciar'.
        if frame > total_frames_potencias
            stop(t);
            set(playButton, 'String', 'Reiniciar');
            disp(['Animación finalizada en t = ', num2str(data_powers.Time(frame-1)), ' s.']);
            return;
        end

        %-------------------------
        % Actualización de la gráfica de potencias
        %-------------------------
        % Se obtiene el tiempo actual (redondeado a 3 decimales) del frame actual.
        current_time = round(data_powers.Time(frame), 3);
        % Se extraen los valores de potencia para las bandas definidas en este frame.
        power_values = table2array(data_powers(frame, frequencies));
        % Se calculan los límites inferior y superior para el eje y, con un margen del 10%.
        y_min = min(power_values) * 0.9;
        y_max = max(power_values) * 1.1;
        % Se actualiza la gráfica de barras con los nuevos valores.
        set(b, 'YData', power_values);
        ylim(hAxes1, [y_min, y_max]);
        title(hAxes1, ['Time: ' num2str(current_time) ' s']);
        xlabel(hAxes1, 'Bandas de Frecuencia');
        ylabel(hAxes1, 'Power (µV^2)');
        set(hAxes1, 'XTickLabel', frequencies);

        %-------------------------
        % Actualización de la gráfica de la FFT
        %-------------------------
        % Se determina el índice central en los datos de signals para el frame actual.
        center_idx = min(1 + (frame - 1) * ratio_muestras, length(time2));
        % Se obtiene el tiempo central correspondiente.
        center_time = time2(center_idx);
        % Se calcula la duración de la mitad de la ventana FFT.
        half_duration = (window_size / Fs) / 2;
        % Se determina el tiempo de inicio y fin de la ventana FFT.
        fft_start_time = center_time - half_duration;
        fft_end_time = center_time + half_duration;

        % Se actualiza cada línea (por canal) en la gráfica de la FFT utilizando los datos precomputados.
        for i = 1:size(signals, 2)
            set(lines(i), 'YData', fft_data{frame, i});
        end

        % Se actualiza el título de la gráfica de la FFT mostrando el intervalo de tiempo actual.
        title(hAxes2, ['FFT desde ', num2str(max(0,fft_start_time), '%.5f'), ' s a ', num2str(fft_end_time, '%.5f'), ' s']);
        xlabel(hAxes2, 'Frecuencia (Hz)');
        ylabel(hAxes2, 'log(Voltios) (μV)');
        legend(hAxes2, 'show', 'Location', 'northeastoutside', 'FontSize', 0.7 * get(gca, 'FontSize'));

        %-------------------------
        % Guardar el frame actual como imagen
        %-------------------------
        % Se construye el nombre del archivo de imagen usando la carpeta de salida y
        % un formato numérico de 5 dígitos.
        frame_filename = fullfile(freqs_folder, sprintf('frame_%05d.png', frame));
        % Se exporta la imagen de la figura actual con una resolución de 200 dpi.
        exportgraphics(hFig, frame_filename, 'Resolution', 200);

        % Se incrementa el contador de frame para el siguiente ciclo.
        frame = frame + 1;
    end

end

%%----------------------------------------------------------------------
%% Función auxiliar: rgb
%%----------------------------------------------------------------------
function c = rgb(color)
% rgb - Convierte un nombre de color (string) a un vector RGB.
% Entradas:
%   color - Nombre del color (ej.: 'blue', 'red', etc.)
% Salida:
%   c     - Vector RGB correspondiente.
    switch color
        case 'blue'
            c = [0 0 1];
        case 'lightgreen'
            c = [0.5 1 0.5];
        case 'red'
            c = [1 0 0];
        case 'orange'
            c = [1 0.5 0];
        case 'yellow'
            c = [1 1 0];
        otherwise
            c = [0 0 0]; % Si el color no coincide, se retorna negro.
    end
end

%%----------------------------------------------------------------------
%% Función para validar el archivo de powers
%%----------------------------------------------------------------------
function [filename_powers, filepath_powers] = validate_file_powers()
% validate_file_powers - Solicita al usuario seleccionar un archivo CSV de powers
% y valida que contenga las columnas necesarias.
%
% Salidas:
%   filename_powers - Nombre del archivo CSV de powers.
%   filepath_powers - Ruta de la carpeta donde se encuentra el archivo.
    [filename, filepath] = uigetfile('*.csv', 'Selecciona el archivo CSV de powers');
    if isequal(filename, 0)
        disp('No se seleccionó ningún archivo.');
        filename_powers = '0';
        filepath_powers = '0';
    else
        fullpath = fullfile(filepath, filename);
        try
            data = readtable(fullpath);
            % Se definen las columnas requeridas para el archivo de powers.
            columnas_necesarias_powers = {'Time','Theta','Alpha','BetaL','BetaH','Gamma'};
            faltantes_powers = setdiff(columnas_necesarias_powers, data.Properties.VariableNames);
            if isempty(faltantes_powers)
                fprintf('El archivo contiene todas las columnas necesarias.\n');
                filename_powers = filename;
                filepath_powers = filepath;
            else
                error('Faltan las siguientes columnas necesarias: %s', strjoin(faltantes_powers, ', '));
                filename_powers = '0';
                filepath_powers = '0';
            end
        catch ME
            error('Error al cargar el archivo: %s', ME.message);
        end
    end
end

%%----------------------------------------------------------------------
%% Función para validar el archivo de signals
%%----------------------------------------------------------------------
function [filename_signals, filepath_signals] = validate_file_signals()
% validate_file_signals - Solicita al usuario seleccionar un archivo CSV de signals
% y valida que contenga las columnas necesarias.
%
% Salidas:
%   filename_signals - Nombre del archivo CSV de signals.
%   filepath_signals - Ruta de la carpeta donde se encuentra el archivo.
    [filename, filepath] = uigetfile('*.csv', 'Selecciona el archivo CSV de señales');
    if isequal(filename, 0)
        disp('No se seleccionó ningún archivo.');
        filename_signals = '0';
        filepath_signals = '0';
    else
        fullpath = fullfile(filepath, filename);
        try
            data = readtable(fullpath);
            % Se definen las columnas requeridas para el archivo de signals.
            columnas_necesarias_signals = {'Time', 'AF3', 'F7', 'F3', 'FC5', 'T7', 'P7', 'O1', 'O2', 'P8', 'T8', 'FC6', 'F4', 'F8', 'AF4', 'MarkerIndex', 'MarkerType', 'MarkerValueInt'};
            faltantes_signals = setdiff(columnas_necesarias_signals, data.Properties.VariableNames);
            if isempty(faltantes_signals)
                fprintf('El archivo contiene todas las columnas necesarias.\n');
                filename_signals = filename;
                filepath_signals = filepath;
            else
                error('Faltan las siguientes columnas necesarias: %s', strjoin(faltantes_signals, ', '));
                filename_signals = '0';
                filepath_signals = '0';
            end
        catch ME
            error('Error al cargar el archivo: %s', ME.message);
        end
    end
end

