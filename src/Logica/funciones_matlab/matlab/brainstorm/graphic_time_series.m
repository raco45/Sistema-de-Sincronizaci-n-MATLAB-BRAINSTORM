function graphic_time_series
    
    brainstorm;
    % Definir el protocolo y sujeto
    protocolName = 'Etapa_A';  % Nombre de tu protocolo
    subjectName = 'Combined';      % Nombre del sujeto

    % Cargar el protocolo
    iProtocol = bst_get('Protocol', protocolName);

    % Obtener el identificador del sujeto
    iSubject = bst_get('Subject', subjectName) 

    %study_neulog = bst_get('Study',4)  
    %study_eeg = bst_get('Study',5)  

    study = bst_get('Study',8)
    datas = study.Data.FileName

    %graph_eeg = study_eeg.Data.FileName
    %data_neulog = study_neulog.Data.FileName

    view_timeseries(datas, 'EEG')
    view_timeseries(datas, 'RGP')
    view_timeseries(datas, 'FC')

    %view_timeseries(graph_eeg)
    %view_timeseries(data_neulog, 'FC')   %,['FC'  'RGP'])   %fc
    %view_timeseries(data_neulog, 'RGP')
    %view_timeseries(data_neulog, ['FC'  'RGP'])
end