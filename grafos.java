import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class grafos {
    private Map<String, List<String>> adjlist;

    public grafos() {
        adjlist = new HashMap<>();
    }

    public void AgregarNodo(String nodo) {
        adjlist.putIfAbsent(nodo, new ArrayList<>());
    }

    public void AgregarArista(String nodo1, String nodo2) throws IllegalArgumentException {
        if (!adjlist.containsKey(nodo1) || !adjlist.containsKey(nodo2)) {
            throw new IllegalArgumentException("Uno o ambos nodos no existen en el grafo.");
        }
        adjlist.get(nodo1).add(nodo2);
        adjlist.get(nodo2).add(nodo1);
    }

    public void MostrarGrafo() {
        for (String nodo : adjlist.keySet()) {
            System.out.println(nodo + " -> " + adjlist.get(nodo));
        }
    }

    // 🔹 Método main para probar
    public static void main(String[] args) {
        grafos g = new grafos();

        g.AgregarNodo("ZonaA");
        g.AgregarNodo("ZonaB");
        g.AgregarNodo("ZonaC");

        g.AgregarArista("ZonaA", "ZonaB");
        g.AgregarArista("ZonaB", "ZonaC");

        g.MostrarGrafo();
    }
}
