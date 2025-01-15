import pandas as pd
import numpy as np
from tkinter import filedialog
import csv

def generate_files():

    columns_interest = [
        'Timestamp',
        'EEG.AF3','EEG.F7','EEG.F3','EEG.FC5','EEG.T7','EEG.P7','EEG.O1','EEG.O2','EEG.P8','EEG.T8','EEG.FC6','EEG.F4','EEG.F8','EEG.AF4',
        'MarkerIndex','MarkerType','MarkerValueInt',
        'POW.AF3.Theta','POW.AF3.Alpha','POW.AF3.BetaL','POW.AF3.BetaH','POW.AF3.Gamma','POW.F7.Theta','POW.F7.Alpha','POW.F7.BetaL','POW.F7.BetaH','POW.F7.Gamma','POW.F3.Theta','POW.F3.Alpha','POW.F3.BetaL','POW.F3.BetaH','POW.F3.Gamma',
        'POW.FC5.Theta','POW.FC5.Alpha','POW.FC5.BetaL','POW.FC5.BetaH','POW.FC5.Gamma','POW.T7.Theta','POW.T7.Alpha','POW.T7.BetaL','POW.T7.BetaH','POW.T7.Gamma','POW.P7.Theta','POW.P7.Alpha','POW.P7.BetaL','POW.P7.BetaH','POW.P7.Gamma',
        'POW.O1.Theta','POW.O1.Alpha','POW.O1.BetaL','POW.O1.BetaH','POW.O1.Gamma','POW.O2.Theta','POW.O2.Alpha','POW.O2.BetaL','POW.O2.BetaH','POW.O2.Gamma','POW.P8.Theta','POW.P8.Alpha','POW.P8.BetaL','POW.P8.BetaH','POW.P8.Gamma',
        'POW.T8.Theta','POW.T8.Alpha','POW.T8.BetaL','POW.T8.BetaH','POW.T8.Gamma','POW.FC6.Theta','POW.FC6.Alpha','POW.FC6.BetaL','POW.FC6.BetaH','POW.FC6.Gamma','POW.F4.Theta','POW.F4.Alpha','POW.F4.BetaL','POW.F4.BetaH','POW.F4.Gamma',
        'POW.F8.Theta','POW.F8.Alpha','POW.F8.BetaL','POW.F8.BetaH','POW.F8.Gamma','POW.AF4.Theta','POW.AF4.Alpha','POW.AF4.BetaL','POW.AF4.BetaH','POW.AF4.Gamma'
    ]

    file_path = filedialog.askopenfilename(
        title="Seleccione el archivo CSV",
        filetypes=[("CSV files", "*.csv")]
    )

    with open(file_path, 'r') as csvfile:
        # Crear un objeto Sniffer
        sniffer = csv.Sniffer()
        # Leer una muestra del archivo
        sample = csvfile.read(1024)
        # Detectar el delimitador
        dialect = sniffer.sniff(sample)
        # Mostrar el delimitador detectado
        print(f"El delimitador detectado es: '{dialect.delimiter}'")
        delimitador = dialect.delimiter


    df = pd.read_csv(file_path,skiprows=1,delimiter=delimitador, low_memory=False)   #elimina la descripcion del sujeto


    if not set(columns_interest).issubset(df.columns):
        print("No se puede procesar: el archivo CSV no contiene todas las columnas requeridas.")
        return


    file_path = file_path.replace('.csv','')

    

    columns_signals = []
    powers = ['Timestamp','MarkerIndex', 'MarkerType', 'MarkerValueInt']
    for x in columns_interest:
        if 'POW' not in x:
            columns_signals.append(x)
        else:
            powers.append(x)

    df_signals = df.loc[:,columns_signals]
    df_powers = df.loc[:,powers]

    initial_time_universal = df_signals.iloc[0,0].replace('.', '')
    initial_time_universal = int(initial_time_universal.replace('.', ''))
    initial_time_universal = initial_time_universal/1e6

    for col in df_signals.columns:
        if 'EEG.' in col:
            df_signals.rename(columns={col: col[4:]}, inplace=True)

    df_signals.rename(columns={'Timestamp': 'Time'}, inplace=True)

    df_signals['Time'] = df_signals['Time'].astype(str).str.replace('.', '')
    df_signals['Time'] = df_signals['Time'].replace(',', '').astype(np.int64)

    df_signals['MarkerIndex'] = df_signals['MarkerIndex'].fillna(0).astype(np.int64)
    df_signals['MarkerType'] = df_signals['MarkerType'].fillna(0).astype(np.int64)
    df_signals['MarkerValueInt'] = df_signals['MarkerValueInt'].fillna(0).astype(np.int64)

    df_signals.iloc[:,1:-3] = df_signals.iloc[:,1:-3].astype(str)
    df_signals.iloc[:,1:-3] = df_signals.iloc[:,1:-3].apply(lambda x: x.str.replace('.', ''))
    df_signals.iloc[:,1:-3] = df_signals.iloc[:,1:-3].apply(lambda x: x.str.replace(',', '')).astype(np.int64)

    df_signals['Time'] = df_signals['Time'].apply(lambda col: col/1e6)

    df_signals['Time'] = round(df_signals['Time'] - initial_time_universal,5)

    df_signals_final = df_signals

    name = file_path + '_SIGNALS.csv'
    df_signals_final.to_csv(name, index=False)

    df_powers.rename(columns={'Timestamp': 'Time'}, inplace=True)

    df_powers['Time'] = df_powers['Time'].astype(str).str.replace('.', '')
    df_powers['Time'] = df_powers['Time'].replace(',', '').astype(np.int64)

    df_powers['MarkerIndex'] = df_powers['MarkerIndex'].fillna(0).astype(np.int64)
    df_powers['MarkerType'] = df_powers['MarkerType'].fillna(0).astype(np.int64)
    df_powers['MarkerValueInt'] = df_powers['MarkerValueInt'].fillna(0).astype(np.int64)

    df_powers = df_powers.dropna()

    df_powers.iloc[:,4:] = df_powers.iloc[:,4:].astype(str)
    df_powers.iloc[:,4:] = df_powers.iloc[:,4:].apply(lambda x: x.str.replace('.', ''))
    df_powers.iloc[:,4:] = df_powers.iloc[:,4:].apply(lambda x: x.str.replace(',', ''))
    df_powers.iloc[:,4:] = df_powers.iloc[:,4:].astype(np.int64)

    df_powers['Time'] = df_powers['Time'].apply(lambda col: col/1e6)

    df_powers['Time'] = round(df_powers['Time'] - initial_time_universal,5)

    # Listas que almacenarán las columns correspondientes
    theta = pd.DataFrame([])
    alpha = pd.DataFrame([])
    betaL = pd.DataFrame([])
    betaH = pd.DataFrame([])
    gamma = pd.DataFrame([])

    # Recorriendo los encabezados para separar las columns según el tipo
    for col in df_powers.columns:
        if 'Theta' in col:
            theta[col] = df_powers[col]
        elif 'Alpha' in col:
            alpha[col] = df_powers[col]
        elif 'BetaL' in col:
            betaL[col] = df_powers[col]
        elif 'BetaH' in col:
            betaH[col] = df_powers[col]
        elif 'Gamma' in col:
            gamma[col] = df_powers[col]

    theta_means = []
    alpha_means = []
    betaL_means = []
    betaH_means = []
    gamma_means = []

    for i in range(0,len(theta)):
        theta_means.append(round(theta.iloc[i,:].mean(),2))
        alpha_means.append(round(alpha.iloc[i,:].mean(),2))
        betaL_means.append(round(betaL.iloc[i,:].mean(),2))
        betaH_means.append(round(betaH.iloc[i,:].mean(),2))
        gamma_means.append(round(gamma.iloc[i,:].mean(),2))


    means = {
        'Theta': theta_means,
        'Alpha': alpha_means,
        'BetaL': betaL_means,
        'BetaH': betaH_means,
        'Gamma': gamma_means
    }

    means2 = pd.DataFrame(means)

    means2.index = df_powers.index
    df_powers_final = df_powers.join(means2)

    df_final2 = df_powers_final

    rows = len(theta)//4

    theta_per_interval = np.zeros(rows)
    alpha_per_interval = np.zeros(rows)
    betaL_per_interval = np.zeros(rows)
    betaH_per_interval = np.zeros(rows)    
    gamma_per_interval = np.zeros(rows)

    time_per_interval = 0.5

    time2 = rows

    times = np.array([])

    for i in range(1,time2+1):   
        times = np.append(0.5*i,times)
    times = np.sort(times)

    for i in range(0,time2):
        theta_per_interval[i] = np.mean(theta_means[4*i+1:4*i+5])
        alpha_per_interval[i] = np.mean(alpha_means[4*i+1:4*i+5])
        betaL_per_interval[i] = np.mean(betaL_means[4*i+1:4*i+5])
        betaH_per_interval[i] = np.mean(betaH_means[4*i+1:4*i+5])
        gamma_per_interval[i] = np.mean(gamma_means[4*i+1:4*i+5])

    power_meaned_path = file_path + '_POWERS_meaned.csv'

    df2 = pd.DataFrame(
        {
            'Time':times,
            'Theta':theta_per_interval,
            'Alpha':alpha_per_interval,
            'BetaL':betaL_per_interval,
            'BetaH':betaH_per_interval,
            'Gamma':gamma_per_interval
        }
    )


    df2.to_csv(power_meaned_path, index=False)
    
    return


generate_files()
