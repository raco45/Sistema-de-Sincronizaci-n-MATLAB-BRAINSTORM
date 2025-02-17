function organizarFiguras(figuraIzqArriba, figuraIzqAbajo, figuraDerArriba, figuraDerAbajo)
    % Obtener el handle de cada figura existente
    fig1 = figuraIzqArriba;
    fig2 = figuraIzqAbajo;
    fig3 = figuraDerArriba;
    fig4 = figuraDerAbajo;
    
    % Posiciones en la pantalla para organizar las figuras
    % Izquierda: figura grande arriba, figura pequeña abajo
    set(fig1, 'Position', [100, 500, 600, 400]);  % Posición y tamaño para la figura izquierda arriba
    set(fig2, 'Position', [100, 100, 400, 300]);  % Posición y tamaño para la figura izquierda abajo
    
    % Derecha: figura pequeña arriba, figura grande abajo
    set(fig3, 'Position', [800, 500, 400, 300]);  % Posición y tamaño para la figura derecha arriba
    set(fig4, 'Position', [800, 100, 600, 400]);  % Posición y tamaño para la figura derecha abajo
end

