function movie_path = frequencies_128Hz(parent_folder, sFile, channelFile)
    % Crear la subcarpeta 'powers' dentro de la carpeta parent
    power_folder = fullfile(parent_folder, 'powers');

    if ~isfolder(power_folder)
        mkdir(power_folder);
        fprintf('Carpeta creada: %s\n', power_folder);
    end

    % Convert raw to CSV
    try
        csvPath = conversor_raw2csv(sFile, channelFile);
    catch ME
        errordlg('CSV Conversion Error', 'Error');
        error('Error in conversor_raw2csv: %s', ME.message);
    end

    % If the conversor returns a empty characters, it stops the execution  
    if isempty(csvPath)
        errordlg('CSV conversion was cancelled or returned empty. Stopping execution', 'Error');
        error('CSV conversion was cancelled or returned empty. Stopping execution');
    end

    % freqs execution
    try
        [outputFolder, fps, name_movie_complete] = freqs(power_folder, csvPath);
    catch ME
        errordlg('Error in freqs function', 'Error');
        error('Error during freqs execution: %s', ME.message);
    end

    % If freqs returns empty values, stop the execution 
    if isempty(outputFolder) || isempty(fps) || isempty(name_movie_complete)
         errordlg('Execution of freqs function was cancelled or returned empty values. Stopping execution', 'Error');
         error('Execution of freqs function was cancelled or returned empty values. Stopping execution');
    end

    % peli_freqs execution
    try
        movie_path = peli_freqs(outputFolder, fps, name_movie_complete);
    catch ME
        errordlg('Error in peli_freqs function', 'Error');
        error('Error during peli_freqs execution: %s', ME.message);
    end

    if isempty(movie_path)
         errordlg('Function peli_freqs returned an empty movie path. Stopping execution', 'Error');
         error('peli_freqs returned an empty movie path. Stopping execution');      
    end
end


function [outputFolder, fps, name_movie_complete] = freqs(power_folder, csvPath)
    set(0, 'DefaultFigureRenderer', 'painters');

    %% 1. Verify and assign CSV file  
    if ~exist(csvPath, 'file')
        errordlg('CSV file not found', 'Error');
        error('CSV file not found: %s', csvPath);
    end
    csv_filename = csvPath;
    
    %% 2. Verify power folder  
    if ~exist(power_folder, 'dir')
        errordlg('Power folder not found', 'Error');
        error('Power folder not found: %s', power_folder);
    end

    %% 3. Request the maximum value for the X-axis for the FFT 
    isValidInput = false;
    while ~isValidInput
        answer = inputdlg('Enter the maximum value for the FFT X-axis (<=64):', ...
                          'X-axis Window', 1, {'64'});
        if isempty(answer)
             errordlg('User cancelled input for X-axis Window. Stopping execution', 'Error');
             error('User cancelled input for X-axis Window. Stopping execution');           
        end
        xAxisWindow = str2double(answer{1});
        if isnan(xAxisWindow)
            warndlg('Invalid input. Please enter a numeric value', 'Input Error');
        elseif xAxisWindow <= 4 || xAxisWindow > 64
            warndlg('The value must be greater than 4 and less than or equal to 64', 'Input Error');
        else
            xAxis_folder = fullfile(power_folder, strcat('powers_', answer{1}));
            
            if ~isfolder(xAxis_folder)
                mkdir(xAxis_folder);
                fprintf('Carpeta creada: %s\n', xAxis_folder);
            else
                % Si la carpeta ya existe, verificar si está vacía
                archivos = dir(xAxis_folder);
                % Filtrar '.' y '..' para ver si hay archivos
                archivos = archivos(~ismember({archivos.name}, {'.', '..'}));
                
                % Si hay archivos, eliminarlos
                if ~isempty(archivos)
                    fprintf('La carpeta %s ya existe y no está vacía. Se procederá a vaciarla.\n', xAxis_folder);
                    for aa = 1:length(archivos)
                        archivo_actual = fullfile(xAxis_folder, archivos(aa).name);
                        delete(archivo_actual);
                    end
                    fprintf('Carpeta %s vaciada exitosamente.\n', xAxis_folder);
                else
                    fprintf('La carpeta %s ya existe pero está vacía. No se realizó ninguna acción.\n', pxAxis_folder);
                end
            end
            outputFolder = xAxis_folder;
            isValidInput = true;
        end
    end
    
    %% 4. Request movie name
    name_movie = inputdlg({'Enter movie name: '}, 'Movie', [1 50], {strcat('movie_', answer{1}, 'Hz')});
    if isempty(name_movie)
        errordlg('User cancelled input for movie name. Stopping execution', 'Error');
        error('User cancelled input for movie name. Stopping execution');
    end
    name_movie_finish = strrep(name_movie{1}, ' ', '_');
    name_movie_finish2 = strrep(name_movie_finish, '.', '_');
    name_movie_complete = strcat(name_movie_finish2, '.avi');
    
    %% 5. Parameters to read CSV file  
    cols_to_use = {'Time','AF3','F7','F3','FC5','T7','P7','O1','O2','P8','T8','FC6','F4','F8','AF4'};
    electrodes = {'AF3','F7','F3','FC5','T7','P7','O1','O2','P8','T8','FC6','F4','F8','AF4'};
    
    % Real Sample Rate (Ts ≈ 0.0078125 s)
    fs = 128;  
    % effectiveXAxisWindow is limited to fs/2 to avoid aliasing 
    effectiveXAxisWindow = min(xAxisWindow, fs/2);
    
    %% 6. FFT and window time parameters  
    % It is fix the size window to 32 samples (~0.25 s) and it is used a pass of 16 samples (~0.125 s)
    window_size = 32;  % 32 samples
    step = 16;         % Steps of 16 samples
    % For the FFT, we use zero-padding to improve the resolution (nfft = 128), but the window keep being of 32 samples 
    nfft = 128;       
    window_type = hann(window_size);  
    noverlap = 0;  % Each FFT is calculated with exactly 32 samples  
    
    %% 7. Define the power bands according to effectiveXAxisWindow
    if effectiveXAxisWindow > 25
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, 12];
        bands.BetaL   = [12, 16];
        bands.BetaH   = [16, 25];
        bands.Gamma   = [25, effectiveXAxisWindow];
        categories    = {'Theta','Alpha','BetaL','BetaH','Gamma'};
    elseif effectiveXAxisWindow > 16
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, 12];
        bands.BetaL   = [12, 16];
        bands.BetaH   = [16, effectiveXAxisWindow];
        categories    = {'Theta','Alpha','BetaL','BetaH'};
    elseif effectiveXAxisWindow > 12
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, 12];
        bands.BetaL   = [12, effectiveXAxisWindow];
        categories    = {'Theta','Alpha','BetaL'};
    elseif effectiveXAxisWindow > 8
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, effectiveXAxisWindow];
        categories    = {'Theta','Alpha'};
    elseif effectiveXAxisWindow > 4
        bands.Theta   = [4, effectiveXAxisWindow];
        categories    = {'Theta'};
    else
        errordlg('xAxisWindow must be greater than 4.', 'Error');
        error('xAxisWindow must be greater than 4.');
    end

    % Colors bars
    bar_colors.Theta = 'blue';
    bar_colors.Alpha = 'green';
    bar_colors.BetaL = 'orange';
    bar_colors.BetaH = 'red';
    bar_colors.Gamma = 'yellow';
    
    %% 8. Read and filter the data
    data = readtable(csv_filename);
    data = data(:, cols_to_use);
    time_data = data.Time; 
    % The number of rows (N) determines the windows
    N = height(data);
    % Calculate the number of completed windows (it is descarted the last rows that dont reach 32 samples)
    num_windows = floor((N - window_size) / step) + 1;
    % Start window vector (in number rows)
    window_starts = 1:step:(num_windows-1)*step+1;
    
    %% 9. Figure settings
    hFig = figure('Name', 'FFT and Band Powers', 'NumberTitle', 'off', ...
                  'MenuBar', 'none', 'ToolBar', 'none', ...
                  'DockControls', 'off', ...
                  'Position', [100, 100, 800, 600]);
    
              
    %% 10. Animation loop (each window is a frame)
    % It is iterated window per window; in each step the FFT is calculated and the power bands
    window_index = 1;
    imageCount = 1;
    % Create power bars and FFT graphic once
    % Inicial state:
    current_state = compute_state(window_index);
    
    % Create the bars graphic 
    ax_power = subplot(2,1,1, 'Parent', hFig);
    catOrder = categorical(categories, categories, 'Ordinal', true);
    b = bar(ax_power, catOrder, cell2mat(struct2cell(current_state.bands))', 'FaceColor','flat');
    for i = 1:numel(categories)
        b.CData(i,:) = rgbFromColorName(bar_colors.(categories{i}));
    end
    ylabel(ax_power, 'Band Power (µV²/Hz)');
    title(ax_power, sprintf('Powers on time: %.3fs', current_state.t_center));
    ylim(ax_power, [0, 1.2*max(cell2mat(struct2cell(current_state.bands)))]);
    
    % Create the FFT graphic
    ax_fft = subplot(2,1,2, 'Parent', hFig);
    hold(ax_fft, 'on');
    cmap = lines(numel(electrodes));
    fft_lines = struct();
    for i = 1:numel(electrodes)
        electrode = electrodes{i};
        [pxx, f] = pwelch(current_state.segment{:, electrode}, window_type, noverlap, nfft, fs);
        valid_freq = f <= effectiveXAxisWindow;
        hLine = plot(ax_fft, f(valid_freq), log10(max(pxx(valid_freq), 1e-12)), 'LineWidth',1.2, 'Color', cmap(i,:));
        fft_lines.(electrode) = hLine;
    end
    hold(ax_fft, 'off');
    xlabel(ax_fft, 'Frequency (Hz)');
    ylabel(ax_fft, 'Amplitude (µV)');
    title(ax_fft, sprintf('log₁₀(FFT) from %.3fs to %.3fs', current_state.t_start, current_state.t_end));
    xlim(ax_fft, [0, effectiveXAxisWindow]);
    xticks(ax_fft, 0:4:effectiveXAxisWindow);
    grid(ax_fft, 'on');
    legend(ax_fft, electrodes, 'Location','eastoutside', 'FontSize',8);
    
    % Main loop. Each iteration process a complete window
    while ishandle(hFig) && (window_index <= num_windows)
        % Calculate the current window
        current_state = compute_state(window_index);
        
        % Update the times (each window steps 0.125 s)
        t_start_disp = (window_index - 1) * 0.125;
        t_end_disp   = t_start_disp + 0.25;
        t_center_disp = t_start_disp + 0.125;
        
        % Update the bars graphic with the power bands of the initial state 
        power_values = cell2mat(struct2cell(current_state.bands))';
        set(b, 'YData', power_values);
        title(ax_power, sprintf('Powers on time: %.3fs', t_center_disp));
        if max(power_values) > 0
            ylim(ax_power, [0, 1.2*max(power_values)]);
        else
            ylim(ax_power, [0, 1]);
        end
        
        % Update the FFT graphic for each electrode 
        for i = 1:numel(electrodes)
            electrode = electrodes{i};
            [pxx, f] = pwelch(current_state.segment{:, electrode}, window_type, noverlap, nfft, fs);
            valid_freq = f <= effectiveXAxisWindow;
            log_psd = log10(max(pxx(valid_freq), 1e-12));
            set(fft_lines.(electrode), 'XData', f(valid_freq), 'YData', log_psd);
        end
        title(ax_fft, sprintf('log₁₀(FFT) from %.3fs to %.3fs', t_start_disp, t_end_disp));
        drawnow;
        try
            exportgraphics(hFig, fullfile(outputFolder, sprintf('frame_%05d.png', imageCount)), 'Resolution', 200);
            imageCount = imageCount + 1;
        catch er
            warning(er.message);
        end
        
        window_index = window_index + 1;

        % Verify if the figure was closed during the animation
        if ~ishandle(hFig)
            errordlg('Figure closed', 'Error');
            error('La figura fue cerrada por el usuario. Ejecución terminada.');
        end

    end


    final_time = time_data(end);
    final_frames = imageCount;
    fps = round(final_frames / final_time, 4);
    if ishandle(hFig)
        close(hFig);
    end
    
    %% Nested functions
    function state = compute_state(win_idx)
        % Calculate the start index using the window_starts vector
        win_start_idx = window_starts(win_idx);
        win_end_idx = win_start_idx + window_size - 1;
        if win_end_idx > N
            errordlg('Not enough data for a complete window', 'Error');
            error('Not enough data for a complete window at window %d.', win_idx);
        end
        segment = data(win_start_idx:win_end_idx, :);
        % Assume the first sample according to t = 0 s.
        t_start = (win_idx - 1) * 0.125;
        t_end = t_start + 0.25;
        t_center = t_start + 0.125;
        state.t_start = t_start;
        state.t_end = t_end;
        state.t_center = t_center;
        state.segment = segment;
        state.bands = compute_band_powers(segment);
    end

    function avg_band_powers = compute_band_powers(segment)
        avg_band_powers = struct();
        for k = 1:numel(categories)
            avg_band_powers.(categories{k}) = [];
        end
        for j = 1:numel(electrodes)
            electrode = electrodes{j};
            signal = segment{:, electrode};
            [pxx, f] = pwelch(signal, window_type, noverlap, nfft, fs);
            for k = 1:numel(categories)
                band_name = categories{k};
                band_range = bands.(band_name);
                idx = f >= band_range(1) & f <= band_range(2);
                if sum(idx) >= 2
                    power = trapz(f(idx), pxx(idx));
                else
                    power = 0;
                end
                avg_band_powers.(band_name) = [avg_band_powers.(band_name), power];
            end
        end
        % Mean each window, give a unique value per band
        for k = 1:numel(categories)
            band_name = categories{k};
            avg_band_powers.(band_name) = mean(avg_band_powers.(band_name));
        end
    end

    function rgb = rgbFromColorName(colorName)
        switch lower(colorName)
            case 'blue'
                rgb = [0 0 1];
            case 'green'
                rgb = [0 0.5 0];
            case 'orange'
                rgb = [1 0.5 0];
            case 'red'
                rgb = [1 0 0];
            case 'yellow'
                rgb = [1 1 0];
            otherwise
                rgb = [0 0 0];
        end
    end
end



function movie_path = peli_freqs(folderPath,fps,name_movie_complete)
    % Specify the path of the folder that contains the frames   
    % Obtain the list of files of desired extension (example: png)
    imageFiles = dir(fullfile(folderPath, '*.png'));

    % Create the VideoWriter object to generate the video   
    outputVideoPath = fullfile(folderPath, name_movie_complete); % Complete path of the video 
    outputVideo = VideoWriter(outputVideoPath);
    outputVideo.FrameRate = fps;
    open(outputVideo);
    
    % Go through each image and write it in the video
    for k = 1:length(imageFiles)
        % Read the image
        filename = fullfile(folderPath, imageFiles(k).name);
        img = imread(filename);
        
        % Optional: If you need that the images have the same size, you can
        % reshape these with imresize

        img = imresize(img, [824, 984]);
        
        % Write the image as frame in the video
        writeVideo(outputVideo, img);
    end
    
    % Close the VideoWriter object and save the video
    close(outputVideo);
    
    % Console message
    fprintf('Video generated successfully in: %s\n', outputVideoPath);

    % Show the message with the video path generated
    msgbox(sprintf('The video was generated successfully in: \n%s', outputVideoPath), ...
           'Video Generated');
    
    movie_path = outputVideoPath;

end


function fullPath_csv = conversor_raw2csv(sFile,channelFile)
  
    % conversor_raw2csv: Export a CSV file the data channels specified, using the electrodes names got of the file "channel.mat"
    %
    % Entries:
    %   sFile - Brainstorm Raw path file (.mat) to process (is loaded with in_bst)
    %   channelFile - Brainstorm Raw path file (.mat) of the channels
    
    %% 1. Load the channel file (channel.mat)
    if ~exist(channelFile, 'file')
        errordlg('Not found the Channel file ', 'Error');
        error('Not found the file %s', channelFile);
    end
    channelsData = load(channelFile);
    
    if ~isfield(channelsData, 'Channel')
        errordlg('The channel.mat do not contain the Channel field', 'Error');
        error('The channel.mat do not contain the Channel field');     
    end

    channelStruct = channelsData.Channel;
    % Extract the list of channel names (and others)
    channelNames = {channelStruct.Name};
    %% 2. Define the channels (electrodes) required
    desiredChannels = {'AF3', 'F7', 'F3', 'FC5', 'T7', 'P7', ...
                       'O1', 'O2', 'P8', 'T8', 'FC6', 'F4', 'F8', 'AF4'};
    
    % Verify that each channel required is on channelNames
    [isFound, idxDesired] = ismember(desiredChannels, channelNames);
    if ~all(isFound)
        missing = desiredChannels(~isFound);
        errordlg('Not found all the channels', 'Error');
        error('Not found the following channels in channel.mat: %s', strjoin(missing, ', '));
        
    end
    
    %% 3. Load the raw file and extract the according data
    % in_bst function load the raw file
    DataMat = in_bst(sFile);
    if ~isfield(DataMat, 'F') || ~isfield(DataMat, 'Time')
        errordlg('The raw file does not contain the F or Time fields', 'Error');
        error('The raw file does not contain the F or Time fields');
    end
    
    % Convert the Time vector to Column
    timeVec = DataMat.Time(:);
    % It is assumed that DataMat.F is a [nChannels x nTimePoints] matrix.
    % We transpose the matrix to obtain [nTimePoints x nChannels]:
    data = DataMat.F';
    
    % IMPORTANT: It is assumed that the order of the rows of channelNames
    % is the same with the rows/columns of DataMat.F
    % It is extracted only the regarding columns of the required channels
    dataSelected = data(:, idxDesired);
    
    %% 4. Create the exported table and write the CSV file
    % Define the header: the first column is 'Time' and then is added the channel names required
    varNames = [{'Time'}, desiredChannels];
    % Combine the Time column with the selected data  
    dataToExport = [timeVec, dataSelected];
    % Create the table with the variables names  
    T = array2table(dataToExport, 'VariableNames', varNames);
    
    % Define the name of the output CSV file
    csvFileName = 'cleaned_file.csv';
    writetable(T, csvFileName);
    
    % Get the path of the generated file  
    fullPath_csv = fullfile(pwd, csvFileName);
    % Show the emergent window with the file path  
    %msgbox(sprintf('Data exported to:\n%s', fullPath_csv), 'Exportation successfully');
end