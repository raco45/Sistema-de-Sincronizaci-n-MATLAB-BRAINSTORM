function posicionarFiguras(fig1, fig2, fig3, fig4)
    % Obtiene el tamaño de la pantalla
    screenSize = get(0, 'ScreenSize');
    screenWidth = screenSize(3);
    screenHeight = screenSize(4);

    % Calcula las posiciones para las figuras
    % fig1: Izquierda (50% de ancho, 75% de alto)
    pos1 = [0, screenHeight * 0.25, screenWidth * 0.5, screenHeight * 0.75];

    % fig4: Abajo izquierda (50% de ancho, 25% de alto)
    pos4 = [0, 0, screenWidth * 0.5, screenHeight * 0.25];

    % fig2: Arriba derecha (50% de ancho, 30% de alto)
    pos2 = [screenWidth * 0.5, screenHeight * 0.6, screenWidth * 0.5, screenHeight * 0.4];

    % fig3: Debajo de fig2 (50% de ancho, ocupa el espacio restante)
    pos3 = [screenWidth * 0.5, 0, screenWidth * 0.5, screenHeight * 0.6];

    % Ajusta las posiciones de las figuras
    set(fig1, 'OuterPosition', pos1);
    set(fig2, 'OuterPosition', pos2);
    set(fig3, 'OuterPosition', pos3);
    set(fig4, 'OuterPosition', pos4);
end
