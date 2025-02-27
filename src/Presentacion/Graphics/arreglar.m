function acomodarFiguras(fig1, fig2, fig3)
    % Obtener el tamaño de la pantalla
    screenSize = get(0, 'ScreenSize');
    screenWidth = screenSize(3);
    screenHeight = screenSize(4);
    
    % Definir dimensiones
    anchoIzq = round(screenWidth * 0.5); % 40% de la pantalla a la izquierda
    anchoDer = round(screenWidth * 0.5); % 60% de la pantalla a la derecha
    altoSup = round(screenHeight * 0.7); % Mitad superior
    altoInf = round(screenHeight * 0.3); % Mitad inferior
    
    % Posicionar cada figura
    set(fig1, 'OuterPosition', [0, altoInf, anchoIzq, altoSup]); % Izquierda arriba
    set(fig2, 'OuterPosition', [0, 0, anchoIzq, altoInf]); % Izquierda abajo
    set(fig3, 'OuterPosition', [anchoIzq, 0, anchoDer, screenHeight]); % Derecha ocupando todo

    % Traer las figuras al frente
    figure(fig1);
    figure(fig2);
    figure(fig3);
end

