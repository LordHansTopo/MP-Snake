package Utilidades;

import java.awt.Color;

public class SelectorColor {
    //Selecciona el color de la serpiente en funcion de su id
    private static final String[] SELECCIONCOLORES = { //Selección de colores
        "#ff0000",
        "#00ff00",
        "#00ff8c",
        "#00ffff",
        "#008cff",
        "#0000ff",
        "#7700ff",
        "#d000ff",
        "#ff00c7",
        "#ff0066",
        "#acbf00",
        "#800000",
        "#805300",
        "#00800c",
        "#008037",
        "#800040",
        "#068000"
    };

    public static Color generarColor(int id) {
        int selector = id % SelectorColor.SELECCIONCOLORES.length;
        return Color.decode(SelectorColor.SELECCIONCOLORES[selector]);
    }
}
