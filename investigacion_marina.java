import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class investigacion_marina {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ventana_principal().setVisible(true));
    }

    static class nodo {
        int id;
        String nombre;
        int x, y;
        java.util.List<String> especies = new ArrayList<>();
        nodo(int id, String nombre, int x, int y) {
            this.id = id;
            this.nombre = nombre;
            this.x = x;
            this.y = y;
        }
        public String toString() { return id + ": " + nombre; }
    }

    static class arista {
        int a, b;
        double peso;
        arista(int a, int b, double peso) { this.a = a; this.b = b; this.peso = peso; }
    }

    static class grafo {
        Map<Integer, nodo> nodos = new HashMap<>();
        Map<Integer, java.util.List<arista>> ady = new HashMap<>();
        int siguiente = 1;

        nodo agregar_nodo(String nombre, int x, int y) {
            nodo n = new nodo(siguiente++, nombre, x, y);
            nodos.put(n.id, n);
            ady.put(n.id, new ArrayList<>());
            return n;
        }

        void agregar_arista(int a, int b, double w) {
            if (!ady.containsKey(a) || !ady.containsKey(b)) return;
            ady.get(a).add(new arista(a, b, w));
            ady.get(b).add(new arista(b, a, w));
        }

        java.util.List<arista> aristas(int id) {
            return ady.getOrDefault(id, Collections.emptyList());
        }

        java.util.List<Integer> bfs(int inicio) {
            java.util.List<Integer> orden = new ArrayList<>();
            if (!nodos.containsKey(inicio)) return orden;
            Set<Integer> vis = new HashSet<>();
            Queue<Integer> q = new LinkedList<>();
            q.add(inicio);
            vis.add(inicio);
            while (!q.isEmpty()) {
                int u = q.poll();
                orden.add(u);
                for (arista e : aristas(u)) {
                    if (!vis.contains(e.b)) {
                        vis.add(e.b);
                        q.add(e.b);
                    }
                }
            }
            return orden;
        }

        static class resultado_dijkstra {
            Map<Integer, Double> dist;
            Map<Integer, Integer> prev;
            resultado_dijkstra(Map<Integer, Double> dist, Map<Integer, Integer> prev) {
                this.dist = dist; this.prev = prev;
            }
        }

        resultado_dijkstra dijkstra(int origen) {
            Map<Integer, Double> dist = new HashMap<>();
            Map<Integer, Integer> prev = new HashMap<>();
            for (int i : nodos.keySet()) dist.put(i, Double.POSITIVE_INFINITY);
            dist.put(origen, 0.0);
            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
            pq.add(new int[]{origen, 0});
            while (!pq.isEmpty()) {
                int[] cur = pq.poll();
                int u = cur[0];
                double du = dist.get(u);
                for (arista e : aristas(u)) {
                    double alt = du + e.peso;
                    if (alt < dist.get(e.b)) {
                        dist.put(e.b, alt);
                        prev.put(e.b, u);
                        pq.add(new int[]{e.b, (int) alt});
                    }
                }
            }
            return new resultado_dijkstra(dist, prev);
        }

        java.util.List<Integer> camino(Map<Integer, Integer> prev, int o, int d) {
            java.util.List<Integer> cam = new ArrayList<>();
            Integer cur = d;
            while (cur != null && !cur.equals(o)) {
                cam.add(cur);
                cur = prev.get(cur);
            }
            if (cur == null) return Collections.emptyList();
            cam.add(o);
            Collections.reverse(cam);
            return cam;
        }
    }

    static class ventana_principal extends JFrame {
        grafo g = new grafo();
        panel_grafo panel;
        JTextArea info;

        ventana_principal() {
            setTitle("investigacion marina");
            setSize(1000, 650);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            panel = new panel_grafo(g, this);
            add(panel, BorderLayout.CENTER);

            JPanel derecha = new JPanel(new BorderLayout());
            derecha.setPreferredSize(new Dimension(300, getHeight()));
            derecha.setBorder(new EmptyBorder(6,6,6,6));

            JPanel controles = new JPanel(new GridLayout(0,1,5,5));

            JButton agregar = new JButton("agregar nodo");
            agregar.addActionListener(e -> {
                String nombre = JOptionPane.showInputDialog(this, "nombre del sitio:");
                if (nombre != null && !nombre.trim().isEmpty()) {
                    g.agregar_nodo(nombre.trim(), 100 + (int)(Math.random()*400), 100 + (int)(Math.random()*300));
                    panel.repaint();
                }
            });
            controles.add(agregar);

            JButton arista = new JButton("agregar arista");
            arista.addActionListener(e -> {
                if (g.nodos.size() < 2) return;
                nodo a = (nodo) JOptionPane.showInputDialog(this, "desde:", "arista", JOptionPane.PLAIN_MESSAGE, null, g.nodos.values().toArray(), null);
                nodo b = (nodo) JOptionPane.showInputDialog(this, "hasta:", "arista", JOptionPane.PLAIN_MESSAGE, null, g.nodos.values().toArray(), null);
                if (a == null || b == null || a.id == b.id) return;
                String sw = JOptionPane.showInputDialog(this, "peso (distancia):", "1.0");
                try {
                    double w = Double.parseDouble(sw);
                    g.agregar_arista(a.id, b.id, w);
                    panel.repaint();
                } catch (Exception ex) {}
            });
            controles.add(arista);

            JButton bfs = new JButton("bfs");
            bfs.addActionListener(e -> {
                nodo inicio = (nodo) JOptionPane.showInputDialog(this, "inicio:", "bfs", JOptionPane.PLAIN_MESSAGE, null, g.nodos.values().toArray(), null);
                if (inicio == null) return;
                java.util.List<Integer> orden = g.bfs(inicio.id);
                StringBuilder sb = new StringBuilder();
                sb.append("recorrido desde ").append(inicio.nombre).append(":\n");
                for (int id : orden) sb.append(g.nodos.get(id).nombre).append("\n");
                info.setText(sb.toString());
            });
            controles.add(bfs);

            JButton dijkstra = new JButton("distancia");
            dijkstra.addActionListener(e -> {
                if (g.nodos.size() < 2) return;
                nodo a = (nodo) JOptionPane.showInputDialog(this, "origen:", "distancia", JOptionPane.PLAIN_MESSAGE, null, g.nodos.values().toArray(), null);
                nodo b = (nodo) JOptionPane.showInputDialog(this, "destino:", "distancia", JOptionPane.PLAIN_MESSAGE, null, g.nodos.values().toArray(), null);
                if (a == null || b == null) return;
                grafo.resultado_dijkstra r = g.dijkstra(a.id);
                java.util.List<Integer> cam = g.camino(r.prev, a.id, b.id);
                if (cam.isEmpty()) info.setText("no hay camino");
                else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("camino más corto:\n");
                    for (int id : cam) sb.append(g.nodos.get(id).nombre).append("\n");
                    sb.append("distancia total: ").append(String.format("%.2f", r.dist.get(b.id)));
                    info.setText(sb.toString());
                    panel.camino_resaltado = cam;
                    panel.repaint();
                }
            });
            controles.add(dijkstra);

            JButton limpiar = new JButton("limpiar");
            limpiar.addActionListener(e -> {
                g = new grafo();
                panel.g = g;
                info.setText("");
                panel.camino_resaltado = new ArrayList<>();
                panel.repaint();
            });
            controles.add(limpiar);

            derecha.add(controles, BorderLayout.NORTH);

            info = new JTextArea();
            info.setEditable(false);
            info.setLineWrap(true);
            JScrollPane sc = new JScrollPane(info);
            derecha.add(sc, BorderLayout.CENTER);

            add(derecha, BorderLayout.EAST);
        }
    }

    static class panel_grafo extends JPanel {
        grafo g;
        ventana_principal padre;
        nodo arrastrando = null;
        int dx, dy;
        java.util.List<Integer> camino_resaltado = new ArrayList<>();

        panel_grafo(grafo g, ventana_principal p) {
            this.g = g;
            this.padre = p;
            setBackground(new Color(10,40,80));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        nodo n = buscar(e.getX(), e.getY());
                        if (n == null) {
                            String nombre = JOptionPane.showInputDialog(panel_grafo.this, "nombre del sitio:");
                            if (nombre != null && !nombre.trim().isEmpty()) {
                                g.agregar_nodo(nombre.trim(), e.getX(), e.getY());
                                repaint();
                            }
                        } else {
                            arrastrando = n;
                            dx = e.getX() - n.x;
                            dy = e.getY() - n.y;
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        nodo n = buscar(e.getX(), e.getY());
                        if (n != null) {
                            JPopupMenu m = new JPopupMenu();
                            JMenuItem ver = new JMenuItem("ver especies");
                            ver.addActionListener(a -> {
                                String msg = n.especies.isEmpty() ? "sin especies registradas" : String.join(", ", n.especies);
                                JOptionPane.showMessageDialog(panel_grafo.this, "sitio: " + n.nombre + "\nespecies: " + msg);
                            });
                            JMenuItem agregar = new JMenuItem("agregar especie");
                            agregar.addActionListener(a -> {
                                String esp = JOptionPane.showInputDialog(panel_grafo.this, "nombre de la especie:");
                                if (esp != null && !esp.trim().isEmpty()) {
                                    n.especies.add(esp.trim());
                                    JOptionPane.showMessageDialog(panel_grafo.this, "especie agregada.");
                                }
                            });
                            m.add(ver);
                            m.add(agregar);
                            m.show(panel_grafo.this, e.getX(), e.getY());
                        }
                    }
                }
                public void mouseReleased(MouseEvent e) { arrastrando = null; }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (arrastrando != null) {
                        arrastrando.x = e.getX() - dx;
                        arrastrando.y = e.getY() - dy;
                        repaint();
                    }
                }
            });
        }

        nodo buscar(int x, int y) {
            for (nodo n : g.nodos.values()) {
                int d = (x - n.x)*(x - n.x) + (y - n.y)*(y - n.y);
                if (d <= 400) return n;
            }
            return null;
        }

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g2 = (Graphics2D) g0;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int u : g.nodos.keySet()) {
                for (arista e : g.aristas(u)) {
                    if (u < e.b) {
                        nodo a = g.nodos.get(e.a);
                        nodo b = g.nodos.get(e.b);
                        boolean resaltado = es_resaltado(u, e.b);
                        g2.setColor(resaltado ? Color.orange : new Color(200,220,255,180));
                        g2.setStroke(new BasicStroke(resaltado ? 4f : 2f));
                        g2.drawLine(a.x, a.y, b.x, b.y);
                        int mx = (a.x + b.x)/2;
                        int my = (a.y + b.y)/2;
                        g2.setColor(Color.white);
                        g2.fillRoundRect(mx-15,my-10,30,18,6,6);
                        g2.setColor(Color.black);
                        g2.drawString(String.format("%.1f", e.peso), mx-10,my+4);
                    }
                }
            }
            for (nodo n : g.nodos.values()) {
                g2.setColor(new Color(40,100,180));
                g2.fillOval(n.x-20, n.y-20, 40, 40);
                g2.setColor(Color.white);
                g2.drawOval(n.x-20, n.y-20, 40, 40);
                g2.drawString(n.nombre, n.x-15, n.y+35);
            }
        }

        boolean es_resaltado(int a, int b) {
            for (int i=0;i<camino_resaltado.size()-1;i++) {
                int u = camino_resaltado.get(i);
                int v = camino_resaltado.get(i+1);
                if ((u==a && v==b)||(u==b && v==a)) return true;
            }
            return false;
        }
    }
}
