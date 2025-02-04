import pandas as pd         # Importa la biblioteca pandas para la manipulación de datos en DataFrames.
import numpy as np          # Importa NumPy para operaciones numéricas y manejo de arreglos.
from tkinter import filedialog  # Importa filedialog de tkinter para mostrar ventanas emergentes de selección de archivos.
import csv                  # Importa el módulo csv para detectar el delimitador y leer archivos CSV.

def generate_files():
    """
    Función generate_files:
    
    Esta función procesa un archivo CSV seleccionado por el usuario y genera dos archivos de salida:
      - Un archivo de señales (_SIGNALS.csv) que contiene las columnas de interés de señales (removiendo el
        prefijo 'EEG.' y ajustando la columna 'Timestamp' a 'Time' normalizada).
      - Un archivo de potencias (_POWERS.csv) que contiene las columnas de potencia (con prefijo 'POW') y
        calcula las medias de cada banda (Theta, Alpha, BetaL, BetaH y Gamma) en intervalos de 0.125 s.
    
    Los pasos que sigue son:
      1. Definir la lista de columnas de interés.
      2. Pedir al usuario que seleccione el archivo CSV mediante una ventana emergente.
      3. Abrir el archivo y, omitiendo la primera línea de metadata, leer una muestra para detectar el delimitador.
      4. Leer el CSV (omitiendo la primera línea) en un DataFrame.
      5. Verificar que el DataFrame contenga todas las columnas requeridas.
      6. Separar las columnas en dos grupos:
           - columns_signals: aquellas que NO contienen 'POW'
           - powers: aquellas que contienen 'POW' (además de columnas base de timestamp y markers)
      7. Procesar el DataFrame de señales (df_signals):
           - Renombrar columnas (eliminar el prefijo 'EEG.' y cambiar 'Timestamp' por 'Time').
           - Ajustar el primer valor de la columna Time (eliminar comas y puntos) y usarlo para normalizar el tiempo.
           - Convertir la columna Time a valores enteros y normalizar (dividir por 1e6 y restar el tiempo inicial).
           - Rellenar valores faltantes en columnas de markers y convertir a enteros.
           - Aplicar el mismo procesamiento (eliminar puntos y comas) al resto de columnas.
           - Guardar el DataFrame resultante en un archivo con sufijo '_SIGNALS.csv'.
      8. Procesar el DataFrame de potencias (df_powers) de forma similar:
           - Renombrar la columna 'Timestamp' a 'Time' y convertirla a entero (eliminando puntos y comas).
           - Rellenar y convertir los markers, eliminar filas con NaN.
           - Aplicar el procesamiento a las columnas de potencias (a partir de la 5ta columna).
           - Normalizar la columna Time.
      9. Separar las columnas de potencias en DataFrames para cada banda: Theta, Alpha, BetaL, BetaH y Gamma.
     10. Calcular la media (redondeada a 2 decimales) de cada banda por fila.
     11. Crear un DataFrame (means2) con estas medias y unirlo a df_powers.
     12. Preparar arreglos de tiempo y de medias (por intervalo de 0.125 s) para construir el DataFrame final.
     13. Guardar el DataFrame final en un archivo CSV con sufijo '_POWERS.csv'.
    """
    # -------------------------------------------------------------------------
    # 1. Definir la lista de columnas de interés (todas se esperan como strings).
    columns_interest = [
        'Timestamp',
        'EEG.AF3','EEG.F7','EEG.F3','EEG.FC5','EEG.T7','EEG.P7','EEG.O1','EEG.O2','EEG.P8','EEG.T8','EEG.FC6','EEG.F4','EEG.F8','EEG.AF4',
        'MarkerIndex','MarkerType','MarkerValueInt',
        'POW.AF3.Theta','POW.AF3.Alpha','POW.AF3.BetaL','POW.AF3.BetaH','POW.AF3.Gamma','POW.F7.Theta','POW.F7.Alpha','POW.F7.BetaL','POW.F7.BetaH','POW.F7.Gamma',
        'POW.F3.Theta','POW.F3.Alpha','POW.F3.BetaL','POW.F3.BetaH','POW.F3.Gamma',
        'POW.FC5.Theta','POW.FC5.Alpha','POW.FC5.BetaL','POW.FC5.BetaH','POW.FC5.Gamma','POW.T7.Theta','POW.T7.Alpha','POW.T7.BetaL','POW.T7.BetaH','POW.T7.Gamma',
        'POW.P7.Theta','POW.P7.Alpha','POW.P7.BetaL','POW.P7.BetaH','POW.P7.Gamma',
        'POW.O1.Theta','POW.O1.Alpha','POW.O1.BetaL','POW.O1.BetaH','POW.O1.Gamma','POW.O2.Theta','POW.O2.Alpha','POW.O2.BetaL','POW.O2.BetaH','POW.O2.Gamma',
        'POW.P8.Theta','POW.P8.Alpha','POW.P8.BetaL','POW.P8.BetaH','POW.P8.Gamma',
        'POW.T8.Theta','POW.T8.Alpha','POW.T8.BetaL','POW.T8.BetaH','POW.T8.Gamma','POW.FC6.Theta','POW.FC6.Alpha','POW.FC6.BetaL','POW.FC6.BetaH','POW.FC6.Gamma',
        'POW.F4.Theta','POW.F4.Alpha','POW.F4.BetaL','POW.F4.BetaH','POW.F4.Gamma',
        'POW.F8.Theta','POW.F8.Alpha','POW.F8.BetaL','POW.F8.BetaH','POW.F8.Gamma','POW.AF4.Theta','POW.AF4.Alpha','POW.AF4.BetaL','POW.AF4.BetaH','POW.AF4.Gamma'
    ]

    # -------------------------------------------------------------------------
    # 2. Solicitar al usuario seleccionar el archivo CSV mediante una ventana emergente.
    file_path = filedialog.askopenfilename(title="Seleccione el archivo CSV", filetypes=[("CSV files", "*.csv")])
    
    # -------------------------------------------------------------------------
    # 3. Detección del delimitador del archivo CSV:
    # Abrir el archivo en modo lectura.
    with open(file_path, 'r') as csvfile:
        csvfile.readline()  # Saltar la primera línea de metadata.
        sample = csvfile.read(1024)  # Leer 1024 caracteres de la segunda línea (cabecera real).
        sniffer = csv.Sniffer()  # Crear un objeto Sniffer para detectar el dialecto.
        dialect = sniffer.sniff(sample)  # Detectar el dialecto (delimitador).
        delimitador = dialect.delimiter  # Extraer el delimitador detectado.

    # -------------------------------------------------------------------------
    # 4. Leer el CSV en un DataFrame de Pandas:
    # Se omite la primera línea (metadata) y se usa el delimitador detectado.
    df = pd.read_csv(file_path, skiprows=1, delimiter=delimitador, low_memory=False)
    
    # -------------------------------------------------------------------------
    # 5. Verificar que el DataFrame contiene todas las columnas de interés.
    if not set(columns_interest).issubset(df.columns):
        print("No se puede procesar: el archivo CSV no contiene todas las columnas requeridas.")
        return

    # -------------------------------------------------------------------------
    # 6. Preparar el nombre base del archivo (sin extensión) para generar archivos de salida.
    file_path = file_path.replace('.csv', '')

    # -------------------------------------------------------------------------
    # 7. Separar columnas en dos grupos:
    # columns_signals: columnas sin 'POW' (señales)
    # powers: columnas de potencia, se añaden columnas base además de las que contienen 'POW'
    columns_signals = []
    powers = ['Timestamp', 'MarkerIndex', 'MarkerType', 'MarkerValueInt']
    for x in columns_interest:
        if 'POW' not in x:
            columns_signals.append(x)
        else:
            powers.append(x)

    # Extraer DataFrames para cada grupo.
    df_signals = df.loc[:, columns_signals]
    df_powers = df.loc[:, powers]

    # -------------------------------------------------------------------------
    # 8. Procesar el DataFrame de señales (df_signals):
    # a) Renombrar columnas para quitar el prefijo "EEG." y cambiar "Timestamp" a "Time".
    df_signals.rename(columns={col: col.replace("EEG.", "") for col in df_signals.columns if col.startswith("EEG.")}, inplace=True)
    df_signals.rename(columns={'Timestamp': 'Time'}, inplace=True)

    # b) Corregir el primer valor de "Time": eliminar comas y puntos, y convertir a entero.
    df_signals.iloc[0, 0] = str(df_signals.iloc[0, 0]).replace(',', '')
    df_signals.iloc[0, 0] = int(str(df_signals.iloc[0, 0]).replace('.', ''))
    # Almacenar este valor como el tiempo inicial universal.
    initial_time_universal = df_signals.iloc[0, 0]
    # Convertir el tiempo inicial de microsegundos a segundos.
    initial_time_universal = initial_time_universal / 1e6

    # c) Procesar la columna 'Time':
    # Convertir a string, eliminar puntos y comas, y luego convertir a int64.
    df_signals['Time'] = df_signals['Time'].astype(str).str.replace('.', '', regex=False)
    df_signals['Time'] = df_signals['Time'].str.replace(',', '', regex=False).astype(np.int64)

    # d) Rellenar los valores faltantes en las columnas de markers y convertir a int64.
    df_signals['MarkerIndex'] = df_signals['MarkerIndex'].fillna(0).astype(np.int64)
    df_signals['MarkerType'] = df_signals['MarkerType'].fillna(0).astype(np.int64)
    df_signals['MarkerValueInt'] = df_signals['MarkerValueInt'].fillna(0).astype(np.int64)

    # e) Para todas las columnas a partir de la segunda, convertir a string, eliminar puntos y comas, y convertir a int64.
    df_signals.iloc[:, 1:] = df_signals.iloc[:, 1:].astype(str)
    df_signals.iloc[:, 1:] = df_signals.iloc[:, 1:].apply(lambda x: x.str.replace('.', '', regex=False))
    df_signals[df_signals.columns[1:]] = df_signals.iloc[:, 1:].apply(lambda x: x.str.replace(',', '', regex=False)).astype(np.int64)

    # f) Normalizar la columna 'Time': convertir de microsegundos a segundos y restar el tiempo inicial.
    df_signals['Time'] = round(df_signals['Time'] / 1e6 - initial_time_universal, 3)

    # g) Eliminar las últimas 3 columnas para obtener el DataFrame final de señales.
    df_signals_final = df_signals
    # Guardar el DataFrame de señales en un archivo CSV con sufijo '_SIGNALS.csv'.
    signal_path = file_path + '_SIGNALS.csv'
    df_signals_final.to_csv(signal_path,, index=False)

    # -------------------------------------------------------------------------
    # 9. Procesar el DataFrame de potencias (df_powers):
    # a) Renombrar "Timestamp" a "Time".
    df_powers.rename(columns={'Timestamp': 'Time'}, inplace=True)

    # b) Convertir la columna 'Time': eliminar puntos y comas y convertir a int64.
    df_powers['Time'] = df_powers['Time'].astype(str).str.replace('.', '', regex=False)
    df_powers['Time'] = df_powers['Time'].str.replace(',', '', regex=False).astype(np.int64)

    # c) Rellenar valores faltantes en las columnas de markers y convertir a int64.
    df_powers['MarkerIndex'] = df_powers['MarkerIndex'].fillna(0).astype(np.int64)
    df_powers['MarkerType'] = df_powers['MarkerType'].fillna(0).astype(np.int64)
    df_powers['MarkerValueInt'] = df_powers['MarkerValueInt'].fillna(0).astype(np.int64)

    # d) Eliminar filas con valores faltantes.
    df_powers = df_powers.dropna()

    # e) Para todas las columnas a partir de la quinta (índice 4), convertir a string, eliminar puntos y comas, y convertir a int64.
    df_powers[df_powers.columns[4:]] = df_powers.iloc[:, 4:].astype(str)
    df_powers[df_powers.columns[4:]] = df_powers.iloc[:, 4:].apply(lambda x: x.str.replace('.', '', regex=False))
    df_powers[df_powers.columns[4:]] = df_powers.iloc[:, 4:].apply(lambda x: x.str.replace(',', '', regex=False)).astype(np.int64)

    # f) Normalizar la columna 'Time' de la misma forma que en df_signals.
    df_powers['Time'] = round(df_powers['Time'] / 1e6 - initial_time_universal, 3)

    # -------------------------------------------------------------------------
    # 10. Separar las columnas de potencias en DataFrames para cada banda.
    # Crear DataFrames vacíos para cada banda.
    theta = pd.DataFrame([])
    alpha = pd.DataFrame([])
    betaL = pd.DataFrame([])
    betaH = pd.DataFrame([])
    gamma = pd.DataFrame([])

    # Recorrer cada columna de df_powers y, según el nombre, asignarla al DataFrame correspondiente.
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

    # -------------------------------------------------------------------------
    # 11. Calcular las medias de cada banda para cada fila.
    theta_means = []
    alpha_means = []
    betaL_means = []
    betaH_means = []
    gamma_means = []

    # Para cada fila en el DataFrame de cada banda, calcular la media y redondearla a 2 decimales.
    for i in range(0, len(theta)):
        theta_means.append(round(theta.iloc[i, :].mean(), 2))
        alpha_means.append(round(alpha.iloc[i, :].mean(), 2))
        betaL_means.append(round(betaL.iloc[i, :].mean(), 2))
        betaH_means.append(round(betaH.iloc[i, :].mean(), 2))
        gamma_means.append(round(gamma.iloc[i, :].mean(), 2))

    # -------------------------------------------------------------------------
    # 12. Crear un DataFrame con las medias calculadas para cada banda.
    means = {
        'Theta': theta_means,
        'Alpha': alpha_means,
        'BetaL': betaL_means,
        'BetaH': betaH_means,
        'Gamma': gamma_means
    }
    means2 = pd.DataFrame(means)
    # Asegurar que el índice de means2 coincide con el de df_powers.
    means2.index = df_powers.index
    # Unir las medias calculadas al DataFrame de potencias.
    df_powers_final = df_powers.join(means2)

    # Se asigna el DataFrame resultante a df_final2 (no se modifica, solo para referencia).
    df_final2 = df_powers_final

    # -------------------------------------------------------------------------
    # 13. Preparar los arreglos de tiempo y de medias por intervalo.
    # Determinar el número de filas (intervalos) a partir de la longitud de theta.
    rows = len(theta)

    # Inicializar arreglos de ceros para almacenar las medias por intervalo para cada banda.
    theta_per_interval = np.zeros(rows)
    alpha_per_interval = np.zeros(rows)
    betaL_per_interval = np.zeros(rows)
    betaH_per_interval = np.zeros(rows)    
    gamma_per_interval = np.zeros(rows)

    # Definir el tiempo por intervalo (0.125 segundos)
    time_per_interval = 0.125

    # Usar el número de filas como cantidad de intervalos.
    time2 = rows

    # Inicializar un arreglo vacío para los tiempos.
    times = np.array([])

    # Para cada intervalo, calcular el tiempo (0.125 * i) y agregarlo al arreglo.
    for i in range(1, time2+1):   
        times = np.append(0.125*i, times)   # Nota: originalmente se usaba 0.5, ahora es 0.125.
    # Ordenar el arreglo de tiempos en orden ascendente.
    times = np.sort(times)

    # Asignar las medias calculadas a cada posición del arreglo correspondiente.
    for i in range(0, time2):   
        theta_per_interval[i] = theta_means[i]
        alpha_per_interval[i] = alpha_means[i]
        betaL_per_interval[i] = betaL_means[i]
        betaH_per_interval[i] = betaH_means[i]
        gamma_per_interval[i] = gamma_means[i]

    # -------------------------------------------------------------------------
    # 14. Crear y guardar el DataFrame final de potencias medias.
    # Definir la ruta del archivo de salida para los datos de potencias medias.
    power_meaned_path = file_path + '_POWERS.csv'

    # Crear un DataFrame con las columnas: Time y las medias de cada banda.
    df2 = pd.DataFrame({
            'Time': times,
            'Theta': theta_per_interval,
            'Alpha': alpha_per_interval,
            'BetaL': betaL_per_interval,
            'BetaH': betaH_per_interval,
            'Gamma': gamma_per_interval
        }
    )

    # Guardar el DataFrame final en un archivo CSV sin incluir el índice.
    df2.to_csv(power_meaned_path, index=False)
    
    return [signal_path, power_meaned_path]

# Ejecutar la función para iniciar el procesamiento.
generate_files()
