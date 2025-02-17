function arrangeThreeFigures(fig1, fig2, fig3)
    % arrangeThreeFigures Organiza 3 figuras en una cuadrícula 2x2, dejando un espacio vacío
    %
    % Parámetros:
    %   fig1, fig2, fig3: Handles de las tres figuras a organizar
    %
    % Ejemplo:
    %   fig1 = figure; plot(rand(10,1));
    %   fig2 = figure; surf(peaks);
    %   fig3 = figure; plot(rand(10,1) * 10);
    %   arrangeThreeFigures(fig1, fig2, fig3);

    % Obtener las figuras proporcionadas
    figures = {fig1, fig2, fig3};
    figures = figures(~cellfun('isempty', figures)); % Filtrar figuras no vacías

    % Verificar que haya exactamente 3 figuras
    if length(figures) ~= 3
        error('Debes proporcionar exactamente 3 figuras como argumentos.');
    end

    % Obtener dimensiones de la pantalla
    screenSize = get(0, 'ScreenSize'); % [x, y, width, height]

    % Calcular ancho y alto de cada ventana
    windowWidth = screenSize(3) / 2; % Mitad del ancho de la pantalla
    windowHeight = screenSize(4) / 2; % Mitad del alto de la pantalla

    % Definir las posiciones de las 3 figuras
    positions = [
        0, screenSize(4) / 2, windowWidth, windowHeight; % Superior izquierda
        screenSize(3) / 2, screenSize(4) / 2, windowWidth, windowHeight; % Superior derecha
        0, 0, windowWidth, windowHeight; % Inferior izquierda
    ];

    % Asignar las posiciones a las figuras
    for i = 1:length(figures)
        set(figures{i}, 'Position', positions(i, :));
    end
end
