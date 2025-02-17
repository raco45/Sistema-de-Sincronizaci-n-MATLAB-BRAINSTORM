function movie_path = frequencies(output_folder, sFile, channelFile)
    % Convert from raw to CSV
    try
        csvPath = conversor_raw2csv(sFile, channelFile);
    catch ME
        errordlg('error', 'CSV Conversion Error');
        error('Error in conversor_raw2csv: %s', ME.message);
    end

    % freqs function execution
    try
        [output_folder, fps, name_movie_complete] = freqs(output_folder, csvPath);
    catch ME
        errordlg('error', 'Error in freqs function');
        error('Error during freqs execution: %s', ME.message);
    end

    % peli_freqs function execution
    try
        movie_path = peli_freqs(output_folder, fps, name_movie_complete);
    catch ME
        errordlg('error', 'Error in peli_freqs function');
        error('Error during peli_freqs execution: %s', ME.message);
    end
end




function [output_folder, fps, name_movie_complete] = freqs(output_folder, csvPath)
    set(0, 'DefaultFigureRenderer', 'painters');

    %% 1. Verify and assign csv file
    if ~exist(csvPath, 'file')
        error('CSV file not found: %s', csvPath);
    end
    csv_filename = csvPath;
    
    %% 2. Verify the output folder
    if ~exist(output_folder, 'dir')
        error('Output folder not found: %s', output_folder);
    end
    outputFolder = output_folder;
    
    %% 3. Request the window value for X-axis of the FFT
    isValidInput = false; % Flag to validate the input
    
    while ~isValidInput
        answer = inputdlg('Enter the maximum value for the FFT X-axis (<=200):', ...
                          'X-axis Window', 1, {'45'});
        
        if isempty(answer)  % If the user cancels or provides empty input
            disp('No value was provided. Exiting.');
            return;
        end
        
        xAxisWindow = str2double(answer{1});  % Convert to a number
        
        % Validate that conversion is successful and the number is in the allowed range
        if isnan(xAxisWindow)
            warndlg('Invalid input. Please enter a numeric value.', 'Input Error');
        elseif xAxisWindow <= 0 || xAxisWindow > 200
            warndlg('The value must be greater than 0 and less than or equal to 200.', 'Input Error');
        else
            isValidInput = true; % Exit the loop if the input is valid
        end
    end
    
    %% 4. Request the movie name
    name_movie = inputdlg({'Enter movie name: '}, 'Movie', [1 50], {strcat('movie_', answer{1}, 'Hz')});
    if isempty(name_movie)
        disp('No movie name provided. Exiting.');
        return;
    end
    if contains(name_movie{1},'.') || contains(name_movie{1},',')
        error('Invalid Name');
    end
    name_movie_finish = strrep(name_movie{1}, ' ', '_');
    name_movie_complete = strcat(name_movie_finish, '.avi');

    %% 5. Parameters for read csv file
    cols_to_use = {'Time','AF3','F7','F3','FC5','T7','P7','O1','O2','P8','T8','FC6','F4','F8','AF4'};
    electrodes = {'AF3','F7','F3','FC5','T7','P7','O1','O2','P8','T8','FC6','F4','F8','AF4'};
    
    % Sample frequency fs
    fs = 2 * xAxisWindow;      
    
    %% 6. Define the FFT parameters 
    nfft = 256;                  % Number of points for the FFT 
    window_type = hann(nfft);    % Hanning window 
    noverlap = 128;              % Number of samples  

    %% 7. Set the power bands according to xAxisWindow
    if xAxisWindow > 30
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, 13];
        bands.BetaL   = [13, 20];
        bands.BetaH   = [20, 30];
        bands.Gamma   = [30, xAxisWindow];
        categories    = {'Theta','Alpha','BetaL','BetaH','Gamma'};
    elseif xAxisWindow > 20
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, 13];
        bands.BetaL   = [13, 20];
        bands.BetaH   = [20, xAxisWindow];  % BetaH from 20 to xAxisWindow
        categories    = {'Theta','Alpha','BetaL','BetaH'};
    elseif xAxisWindow > 13
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, 13];
        bands.BetaL   = [13, xAxisWindow];    % BetaL from 13 to xAxisWindow
        categories    = {'Theta','Alpha','BetaL'};
    elseif xAxisWindow > 8
        bands.Theta   = [4, 8];
        bands.Alpha   = [8, xAxisWindow];      % Alpha from 8 to xAxisWindow
        categories    = {'Theta','Alpha'};
    elseif xAxisWindow > 4
        bands.Theta   = [4, xAxisWindow];        % Only Theta available
        categories    = {'Theta'};
    else
        error('xAxisWindow must be greater than 4.');
    end

    % Colors for the bars 
    bar_colors.Theta = 'blue';
    bar_colors.Alpha = 'green';
    bar_colors.BetaL = 'orange';
    bar_colors.BetaH = 'red';
    bar_colors.Gamma = 'yellow';
    
    %% 8. Read and filter data
    data = readtable(csv_filename);
    data = data(:, cols_to_use);
    time_data = data.Time;  
    
    %% 9. Set the sliding window
    window_size = 256;            % Segment size
    step = window_size - noverlap;  
    N = height(data);
    window_starts = 1:step:(N - window_size + 1);
    
    %% 10. Figure settings
    hFig = figure('Name', 'FFT and Band Powers', 'NumberTitle', 'off', ...
                  'MenuBar', 'none', 'ToolBar', 'none', ...
                  'DockControls', 'off', ...
                  'Position', [100, 100, 800, 600]);
              
    %% 11. States initialization and interpolation parameters
    window_index = 1;
    current_state = compute_state(window_index);
    if length(window_starts) >= 2
        next_state = compute_state(window_index+1);
    else
        next_state = current_state;
    end
    n_interp = 8;  % 8 frames per transition (0.125 s por frame)
    
    %% 12. Figure settings with two subplots
    clf(hFig);
    % Upper subplot: graphic bar of power bands
    ax_power = subplot(2,1,1, 'Parent', hFig);
    catOrder = categorical(categories, categories, 'Ordinal', true);
    initial_values = zeros(1, numel(categories));
    for i = 1:numel(categories)
        initial_values(i) = current_state.bands.(categories{i});
    end
    b = bar(ax_power, catOrder, initial_values, 'FaceColor','flat');
    for i = 1:numel(categories)
        b.CData(i,:) = rgbFromColorName(bar_colors.(categories{i}));
    end
    ylabel(ax_power, 'Band Power (µV²/Hz)');
    title(ax_power, sprintf('Powers on time: %.2fs', current_state.t_center));
    if max(initial_values) > 0
        ylim(ax_power, [0, 1.2*max(initial_values)]);
    else
        ylim(ax_power, [0, 1]);
    end
    
    % Lower subplot: log10(FFT) graphic
    ax_fft = subplot(2,1,2, 'Parent', hFig);
    hold(ax_fft, 'on');
    cmap = lines(numel(electrodes));
    fft_lines = struct();
    segment_init = current_state.segment;
    for i = 1:numel(electrodes)
        electrode = electrodes{i};
        signal = segment_init{:, electrode};
        [pxx, f] = pwelch(signal, window_type, noverlap, nfft, fs);
        valid_freq = f <= xAxisWindow;
        f_filtered = f(valid_freq);
        pxx_filtered = pxx(valid_freq);
        log_psd = log10(max(pxx_filtered, 1e-12));
        hLine = plot(ax_fft, f_filtered, log_psd, 'LineWidth',1.2, 'Color', cmap(i,:));
        fft_lines.(electrode) = hLine;
    end
    hold(ax_fft, 'off');
    xlabel(ax_fft, 'Frequency (Hz)');
    ylabel(ax_fft, 'Amplitude (µV)');
    title(ax_fft, sprintf('log₁₀(FFT) from %.2fs to %.2fs', current_state.t_start, current_state.t_end));
    xlim(ax_fft, [0, xAxisWindow]);
    xticks(ax_fft, 0:4:xAxisWindow);
    grid(ax_fft, 'on');
    legend(ax_fft, electrodes, 'Location','eastoutside', 'FontSize',8);
    
    %% 13. Counter initialization to capture images
    imageCount = 1;
    
    %Repeat the firsts 8 frames from initial state
    for i = 1:8
        if ishandle(hFig)
            try
                exportgraphics(hFig, fullfile(outputFolder, sprintf('frame_%05d.png', imageCount)), 'Resolution', 200);
                imageCount = imageCount + 1;
            catch er
                warning('Error exporting initial frame: %s', E.message);
            end
            drawnow;
        end
    end
    
    %% 14. Animation loop (Avoid duplicated frames)  
    while ishandle(hFig)
        %Generate only n_interp frames (without repeating the intial frame) 
        for frame = 1:n_interp
            if ~ishandle(hFig)
                error('Figure closed.');
            end
            
            alpha = frame / n_interp;  % alpha varies from 1/n_interp to 1
            % Bars power interpolation   
            current_values = zeros(1, numel(categories));
            for i = 1:numel(categories)
                cat = categories{i};
                v_current = current_state.bands.(cat);
                v_next = next_state.bands.(cat);
                current_values(i) = (1 - alpha)*v_current + alpha*v_next;
            end
            b.YData = current_values;
            if max(current_values) > 0
                ylim(ax_power, [0, 1.2 * max(current_values)]);
            else
                ylim(ax_power, [0, 1]);
            end
            
            % Time interpolation 
            t_start_interp = (1 - alpha)*current_state.t_start + alpha*next_state.t_start;
            t_end_interp   = (1 - alpha)*current_state.t_end   + alpha*next_state.t_end;
            t_center_interp = (1 - alpha)*current_state.t_center + alpha*next_state.t_center;
            
            title(ax_power, sprintf('Powers on time: %.2fs', t_center_interp));
            title(ax_fft, sprintf('log₁₀(FFT) from %.2fs to %.2fs', t_start_interp, t_end_interp));
            
            % Recalculate FFT for the interpolated frame    
            for i = 1:numel(electrodes)
                electrode = electrodes{i};
                signal1 = current_state.segment{:, electrode};
                signal2 = next_state.segment{:, electrode};
                interp_signal = (1 - alpha)*signal1 + alpha*signal2;
                [pxx, f] = pwelch(interp_signal, window_type, noverlap, nfft, fs);
                valid_freq = f <= xAxisWindow;
                f_filtered = f(valid_freq);
                pxx_filtered = pxx(valid_freq);
                log_psd = log10(max(pxx_filtered, 1e-12));
                set(fft_lines.(electrode), 'XData', f_filtered, 'YData', log_psd);
            end
            xlim(ax_fft, [0, xAxisWindow]);
            xticks(ax_fft, 0:4:xAxisWindow);
            
            drawnow;
            
            if ishandle(hFig)
                try
                    exportgraphics(hFig, fullfile(outputFolder, sprintf('frame_%05d.png', imageCount)), 'Resolution', 200);
                    imageCount = imageCount + 1;
                catch er
                    warning('Error when exporting the image: %s', E.message);
                end
            end
        end

        % Update states for the next window    
        window_index = window_index + 1;
        if window_index > length(window_starts)
            break;
        end
        current_state = next_state;
        if window_index < length(window_starts)
            next_state = compute_state(window_index+1);
        else
            next_state = current_state;
        end
    end

    % Repeat the last frame 8 times
    for i = 1:8
        if ishandle(hFig)
            try
                exportgraphics(hFig, fullfile(outputFolder, sprintf('frame_%05d.png', imageCount)), 'Resolution', 200);
                imageCount = imageCount + 1;
            catch er
                warning('Error exporting final frame: %s', E.message);
            end
            drawnow;
        end
    end
    
    final_time = time_data(end);
    final_frames = imageCount - 1;
    
    fps = round(final_frames/final_time, 4);

    if ishandle(hFig)
        close(hFig);
    end
    
    %% Nested function 
    function state = compute_state(win_idx)
        start_idx = window_starts(win_idx);
        end_idx = start_idx + window_size - 1;
        segment = data(start_idx:end_idx, :);
        bands_struct = compute_band_powers(segment);
        t_start = segment.Time(1);
        t_end = segment.Time(end);
        t_center = (t_start + t_end) / 2;
        state.bands = bands_struct;
        state.t_start = t_start;
        state.t_end = t_end;
        state.t_center = t_center;
        state.segment = segment;
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
                power = trapz(f(idx), pxx(idx));
                if isempty(avg_band_powers.(band_name))
                    avg_band_powers.(band_name) = power;
                else
                    avg_band_powers.(band_name) = [avg_band_powers.(band_name), power];
                end
            end
        end
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
    %PATHS EXAMPLES
    %sFile = 'C:\Users\Usuario\OneDrive\Documentos\brainstorm_db\Roman_febrero\data\Roman1/@rawsenales_EtapaA_arreglados_desde_colab_22-08_band_notch_cutstim/data_0raw_senales_EtapaA_arreglados_desde_colab_22-08_band_notch_cutstim.mat';
    %channelFile = 'C:\Users\Usuario\OneDrive\Documentos\brainstorm_db\Roman_febrero\data\Roman1/@rawsenales_EtapaA_arreglados_desde_colab_22-08_band_notch_cutstim/channel.mat'

    % conversor_raw2csv: Export a CSV file the data channels specified, using the electrodes names got of the file "channel.mat"
    %
    % Entries:
    %   sFile - Raw path file (.mat) to process (is loaded with in_bst)
    %   channelFile - Raw path file (.mat) of the channels
    
    %% 1. Load the channel file (channel.mat)
    if ~exist(channelFile, 'file')
        error('Not found the file %s', channelFile);
    end
    channelsData = load(channelFile);
    
    if ~isfield(channelsData, 'Channel')
        error('The channel.mat do not contain the Channel field')
    end

    channelStruct = channelsData.Channel;
    disp(channelStruct)
    % Extract the list of channel names (and others)
    channelNames = {channelStruct.Name};
    disp(channelNames)
    %% 2. Define the channels (electrodes) required
    desiredChannels = {'AF3', 'F7', 'F3', 'FC5', 'T7', 'P7', ...
                       'O1', 'O2', 'P8', 'T8', 'FC6', 'F4', 'F8', 'AF4'};
    
    % Verify that each channel required is on channelNames
    [isFound, idxDesired] = ismember(desiredChannels, channelNames);
    if ~all(isFound)
        missing = desiredChannels(~isFound);
        error('Not found the following channels in channel.mat: %s', strjoin(missing, ', '));
    end
    
    %% 3. Load the raw file and extract the according data
    % in_bst function load the raw file
    DataMat = in_bst(sFile);
    if ~isfield(DataMat, 'F') || ~isfield(DataMat, 'Time')
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
    
    disp(['Data exported to: ', csvFileName]);
    
    % Get the path of the generated file  
    fullPath_csv = fullfile(pwd, csvFileName);
    % Show the emergent window with the file path  
    %msgbox(sprintf('Data exported to:\n%s', fullPath_csv), 'Exportation successfully');
end