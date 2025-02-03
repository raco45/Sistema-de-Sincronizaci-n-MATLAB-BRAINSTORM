function graphic_powers_and_FFT_optimized(file1, file2)
    % Leer los datos del primer archivo (potencias)
    opts = detectImportOptions(file1, 'VariableNamingRule', 'preserve');
    data1 = readtable(file1, opts);

    % Leer los datos del segundo archivo (señales)
    data2 = readtable(file2);

    % Extraer columnas necesarias
    time2 = round(data2.Time, 2);
    signals = data2{:, 2:15};
    electrode_names = data2.Properties.VariableNames(2:end);

    % Parámetros
    Fs = 256;
    N = length(time2);
    window_size = 0.5 * Fs;
    step_size = 0.25 * Fs;
    frequencies2 = (0:window_size-1) * (Fs/window_size);
    half_range = 1:floor(window_size/2);
    frequencies2 = frequencies2(half_range);

    % Bandas y colores
    frequencies = {'Theta', 'Alpha', 'BetaL', 'BetaH', 'Gamma'};
    colors = {'blue', 'lightgreen', 'red', 'orange', 'yellow'};

    % Precalcular FFTs
    total_frames2 = floor((N - window_size) / step_size) + 1;
    fft_data = cell(total_frames2, size(signals, 2)); % Almacenar FFTs precomputadas
    for frame2 = 1:total_frames2
        start_idx = 1 + (frame2 - 1) * step_size;
        window_signals = signals(start_idx:start_idx+window_size-1, :);
        for i = 1:size(window_signals, 2)
            fft_signal = fft(window_signals(:, i));
            magnitude = abs(fft_signal / window_size);
            fft_data{frame2, i} = 10 * log10(magnitude(half_range) + eps); % Magnitud log
        end
    end

    % Configuración de la figura y subplots
    hFig = figure('Name', 'FFT y potencias', 'NumberTitle', 'off', ...
              'MenuBar', 'none', 'ToolBar', 'none', 'DockControls', 'off'); % Desactivar menús y barras
    hAxes1 = subplot(2,1,1);
    hAxes2 = subplot(2,1,2);

    % Crear barras y líneas para actualizar directamente
    b = bar(hAxes1, zeros(1, length(frequencies)), 'FaceColor', 'flat');
    for i = 1:length(frequencies)
        b.CData(i, :) = rgb(colors{i});
    end
    lines = gobjects(1, size(signals, 2));
    for i = 1:size(signals, 2)
        lines(i) = plot(hAxes2, frequencies2, zeros(size(frequencies2)), 'DisplayName', electrode_names{i});
        hold(hAxes2, 'on');
    end
    hold(hAxes2, 'off');
    
    % Botón Play/Pause/Reiniciar
    isPlaying = false; % Inicializamos en pausa
    setappdata(hFig, 'isPlaying', isPlaying);
    playButton = uicontrol('Style', 'togglebutton', 'String', 'Play', ...
                           'Position', [10 10 60 30], ...
                           'Callback', @playPauseCallback);

    % Inicialización de variables de iteración
    frame = 1;
    frame2 = 1;

    % Timer para actualizar las gráficas
    t = timer('ExecutionMode', 'fixedRate', 'Period', 0.5, ...
              'TimerFcn', @updateGraphics, 'StartDelay', Inf); % Inicia detenido

    % Función para manejar el botón Play/Pause/Reiniciar
    function playPauseCallback(src, ~)
        isPlaying = getappdata(hFig, 'isPlaying');
        if isPlaying
            stop(t);
            set(src, 'String', 'Play');
        else
            if frame > total_frames2 % Si ya hemos llegado al final, reiniciar
                frame = 1;
                frame2 = 1;
                set(src, 'String', 'Pause');
                start(t); % Reiniciar la animación
            else
                start(t);
                set(src, 'String', 'Pause');
            end
        end
        setappdata(hFig, 'isPlaying', ~isPlaying);
    end

    % Función para actualizar las gráficas
    function updateGraphics(~, ~)
        if frame > height(data1) || frame2 > total_frames2 || ~ishandle(hFig)
            stop(t);
            set(playButton, 'String', 'Reiniciar'); % Cambiar texto a "Reiniciar"
            disp('Animación finalizada.');
            return;
        end

        % Actualizar gráfica 1 (Potencias)
        current_time = round(data1.Time(frame), 2);
        set(b, 'YData', table2array(data1(frame, frequencies)));
        ylim(hAxes1, [0, max(max(table2array(data1(:, frequencies)))) * 1.01]);
        title(hAxes1, ['Time: ' num2str(current_time) ' s']);
        ylabel(hAxes1, 'Power (µV^2)');
        xlabel(hAxes1, 'Bandas de Frecuencia');
        set(hAxes1, 'XTickLabel', frequencies);

        % Actualizar gráfica 2 (STFT)
        for i = 1:size(signals, 2)
            set(lines(i), 'YData', fft_data{frame2, i});
        end
        start_time = time2(1 + (frame2 - 1) * step_size);
        end_time = time2(min(1 + (frame2 - 1) * step_size + window_size - 1, length(time2)));
        title(hAxes2, ['STFT desde ', num2str(start_time, '%.1f'), ' s a ', num2str(end_time, '%.1f'), ' s']);
        xlabel(hAxes2, 'Frecuencia (Hz)');
        ylabel(hAxes2, 'log(Voltios) (μV)');
        legend(hAxes2, 'show', 'Location', 'northeastoutside', 'FontSize', 0.40 * get(gca, 'FontSize')); % Cambiar tamaño de fuente

        % Incrementar los índices
        frame = frame + 1;
        frame2 = frame2 + 1;
    end
end

% Función auxiliar para convertir colores a RGB
function c = rgb(color)
    switch color
        case 'blue', c = [0 0 1];
        case 'lightgreen', c = [0.5 1 0.5];
        case 'red', c = [1 0 0];
        case 'orange', c = [1 0.5 0];
        case 'yellow', c = [1 1 0];
        otherwise, c = [0 0 0];
    end
end
