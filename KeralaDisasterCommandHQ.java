import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

// =============================================================================
// 1. TACTICAL DESIGN TOKENS
// =============================================================================
class TacticalTheme {
    public static final Color BG_VOID          = new Color(13, 15, 18);     // #0D0F12 Deep Charcoal
    public static final Color PANEL_SURFACE    = new Color(22, 26, 32);     // #161A20 Component Graphite
    public static final Color PANEL_ELEVATED   = new Color(30, 36, 45);     // #1E242D Hover/Elevated
    public static final Color BORDER_DIM       = new Color(45, 55, 68);     // #2D3744 Crisp Borders
    public static final Color ACCENT_EMERALD   = new Color(0, 255, 163);    // #00FFA3 Primary Ops Live
    public static final Color ACCENT_CYAN      = new Color(0, 210, 255);    // #00D2FF Telemetry Data
    public static final Color ACCENT_AMBER     = new Color(255, 184, 0);    // #FFB800 Caution Multiplier
    public static final Color ACCENT_CRIMSON   = new Color(255, 59, 48);    // #FF3B30 Red Alert / Shortage
    public static final Color TEXT_PRIMARY     = new Color(245, 247, 250);
    public static final Color TEXT_SECONDARY   = new Color(140, 150, 165);

    public static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY         = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD         = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO_BIG     = new Font("Consolas", Font.BOLD, 26);
    public static final Font FONT_MONO_MED     = new Font("Consolas", Font.BOLD, 14);
    public static final Font FONT_MONO_SMALL   = new Font("Consolas", Font.PLAIN, 11);

    public static Border createPanelBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DIM, 1),
            new EmptyBorder(12, 14, 12, 14)
        );
    }
}

// =============================================================================
// 2. VECTOR RADAR LOGO & TACTICAL GRAPHICS
// =============================================================================
class HexRadarBadge extends JPanel {
    private double sweepAngle = 0;

    public HexRadarBadge(int size) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setOpaque(false);

        // Continuous 30 FPS Radar Sweep
        Timer radarTimer = new Timer(33, e -> {
            sweepAngle += 0.06;
            if (sweepAngle >= Math.PI * 2) sweepAngle = 0;
            repaint();
        });
        radarTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int radius = Math.min(cx, cy) - 4;

        // Draw Outer Hexagon
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            hex.addPoint((int) (cx + radius * Math.cos(angle)), (int) (cy + radius * Math.sin(angle)));
        }
        g2.setColor(TacticalTheme.BORDER_DIM);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(hex);

        // Draw Internal Concentric Radar Rings
        g2.setColor(new Color(0, 255, 163, 40));
        g2.drawOval(cx - radius / 2, cy - radius / 2, radius, radius);

        // Draw Sweeping Radar Beam
        int endX = (int) (cx + (radius - 2) * Math.cos(sweepAngle));
        int endY = (int) (cy + (radius - 2) * Math.sin(sweepAngle));
        g2.setColor(TacticalTheme.ACCENT_EMERALD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(cx, cy, endX, endY);

        // Core Center Target Dot
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 3, cy - 3, 6, 6);

        g2.dispose();
    }
}

// Custom Geospatial Tactical Map
class TacticalKeralaMapCanvas extends JPanel {
    static class MapSectorNode {
        String code;
        String name;
        int x, y;
        int stockPercent;
        boolean hasDisaster;

        MapSectorNode(String code, String name, int x, int y, int stockPercent, boolean hasDisaster) {
            this.code = code;
            this.name = name;
            this.x = x;
            this.y = y;
            this.stockPercent = stockPercent;
            this.hasDisaster = hasDisaster;
        }
    }

    private final List<MapSectorNode> sectors = new ArrayList<>();
    private MapSectorNode hoveredSector = null;

    public TacticalKeralaMapCanvas() {
        setOpaque(false);
        setupNodes();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                MapSectorNode found = null;
                for (MapSectorNode node : sectors) {
                    if (e.getPoint().distance(node.x, node.y) <= 15) {
                        found = node;
                        break;
                    }
                }
                if (found != hoveredSector) {
                    hoveredSector = found;
                    setCursor(hoveredSector != null ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });
    }

    private void setupNodes() {
        sectors.clear();
        sectors.add(new MapSectorNode("SEC-01", "Kasaragod Grid", 80, 50, 85, false));
        sectors.add(new MapSectorNode("SEC-02", "Kannur Outpost", 110, 95, 78, false));
        sectors.add(new MapSectorNode("SEC-03", "Wayanad (Meppadi Camp)", 160, 140, 32, true));
        sectors.add(new MapSectorNode("SEC-04", "Kozhikode Distribution", 145, 185, 90, false));
        sectors.add(new MapSectorNode("SEC-05", "Thrissur Emergency Depot", 200, 230, 64, false));
        sectors.add(new MapSectorNode("SEC-06", "Idukki (Highland Relief)", 255, 270, 41, true));
        sectors.add(new MapSectorNode("SEC-07", "Kochi Central Command Hub", 220, 300, 95, false));
        sectors.add(new MapSectorNode("SEC-08", "Alappuzha (Kuttanad Sector)", 250, 350, 55, false));
        sectors.add(new MapSectorNode("SEC-09", "Thiruvananthapuram HQ", 295, 420, 92, false));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background Coordinate Grid
        g2.setColor(new Color(255, 255, 255, 8));
        for (int i = 0; i < getWidth(); i += 40) g2.drawLine(i, 0, i, getHeight());
        for (int i = 0; i < getHeight(); i += 40) g2.drawLine(0, i, getWidth(), i);

        // Route Interlinks
        for (int i = 0; i < sectors.size() - 1; i++) {
            MapSectorNode s1 = sectors.get(i);
            MapSectorNode s2 = sectors.get(i + 1);

            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 4}, 0));
            g2.setColor(s1.hasDisaster || s2.hasDisaster ? TacticalTheme.ACCENT_CRIMSON : TacticalTheme.ACCENT_CYAN);
            g2.drawLine(s1.x, s1.y, s2.x, s2.y);
        }

        // Sector Nodes
        for (MapSectorNode s : sectors) {
            Color statusColor = s.hasDisaster ? TacticalTheme.ACCENT_CRIMSON : TacticalTheme.ACCENT_EMERALD;

            // Outer Target Reticle
            g2.setColor(new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), 60));
            g2.drawOval(s.x - 10, s.y - 10, 20, 20);

            // Core Point
            g2.setColor(statusColor);
            g2.fillOval(s.x - 4, s.y - 4, 8, 8);

            // Code & Name
            g2.setFont(TacticalTheme.FONT_MONO_SMALL);
            g2.setColor(TacticalTheme.TEXT_SECONDARY);
            g2.drawString(s.code, s.x + 14, s.y - 3);

            g2.setFont(TacticalTheme.FONT_BOLD);
            g2.setColor(TacticalTheme.TEXT_PRIMARY);
            g2.drawString(s.name, s.x + 14, s.y + 11);
        }

        // Live Hover Telemetry Card
        if (hoveredSector != null) {
            int cardW = 200, cardH = 70;
            int tx = Math.min(hoveredSector.x + 15, getWidth() - cardW - 10);
            int ty = Math.max(hoveredSector.y - cardH - 10, 10);

            g2.setColor(TacticalTheme.PANEL_SURFACE);
            g2.fillRect(tx, ty, cardW, cardH);
            g2.setColor(hoveredSector.hasDisaster ? TacticalTheme.ACCENT_CRIMSON : TacticalTheme.ACCENT_EMERALD);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(tx, ty, cardW, cardH);

            g2.setFont(TacticalTheme.FONT_BOLD);
            g2.setColor(TacticalTheme.TEXT_PRIMARY);
            g2.drawString(hoveredSector.name, tx + 10, ty + 18);

            g2.setFont(TacticalTheme.FONT_MONO_SMALL);
            g2.setColor(TacticalTheme.TEXT_SECONDARY);
            g2.drawString("SECTOR: " + hoveredSector.code, tx + 10, ty + 34);

            g2.setColor(hoveredSector.hasDisaster ? TacticalTheme.ACCENT_CRIMSON : TacticalTheme.ACCENT_CYAN);
            g2.drawString(hoveredSector.hasDisaster ? "STATE: RED ALERT (CRITICAL SHORTAGE)" : "STATE: NORMAL REPLENISHMENT", tx + 10, ty + 50);
            g2.drawString("CURRENT CAPACITY: " + hoveredSector.stockPercent + "%", tx + 10, ty + 64);
        }

        g2.dispose();
    }
}

// Circular Fulfilment Gauge
class TacticalArcGauge extends JPanel {
    private int score = 74;

    public TacticalArcGauge() {
        setOpaque(false);
        setPreferredSize(new Dimension(140, 140));
    }

    public void setScore(int score) {
        this.score = score;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        int stroke = 10;

        // Base Track
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.setColor(TacticalTheme.BORDER_DIM);
        g2.drawArc(x + stroke/2, y + stroke/2, size - stroke, size - stroke, -30, 240);

        // Colored Filled Arc
        g2.setColor(TacticalTheme.ACCENT_EMERALD);
        int sweep = (int) (240 * (score / 100.0));
        g2.drawArc(x + stroke/2, y + stroke/2, size - stroke, size - stroke, 210, -sweep);

        // Metric Readout
        g2.setFont(TacticalTheme.FONT_MONO_BIG);
        g2.setColor(TacticalTheme.TEXT_PRIMARY);
        String s = score + "%";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, getWidth() / 2 - fm.stringWidth(s) / 2, getHeight() / 2 + 6);

        g2.setFont(TacticalTheme.FONT_MONO_SMALL);
        g2.setColor(TacticalTheme.TEXT_SECONDARY);
        String label = "STATE METRIC";
        FontMetrics fms = g2.getFontMetrics();
        g2.drawString(label, getWidth() / 2 - fms.stringWidth(label) / 2, getHeight() / 2 + 22);

        g2.dispose();
    }
}

// =============================================================================
// 3. MAIN FRONTEND COMMAND APPLICATION
// =============================================================================
public class KeralaDisasterCommandHQ extends JFrame {

    private final CardLayout mainCardLayout;
    private final JPanel mainWorkspace;
    private final List<JButton> navPills = new ArrayList<>();
    private final JLabel lblClock;

    public KeralaDisasterCommandHQ() {
        setTitle("KERALA DISASTER RESOURCE COMMAND CENTER // SDMA AIR-GAP PORT");
        setSize(1380, 840);
        setMinimumSize(new Dimension(1180, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(TacticalTheme.BG_VOID);
        setLayout(new BorderLayout());

        // 1. Top Command Header
        add(createTopCommandHeader(), BorderLayout.NORTH);

        // 2. Central Display Deck
        mainCardLayout = new CardLayout();
        mainWorkspace = new JPanel(mainCardLayout);
        mainWorkspace.setOpaque(false);

        mainWorkspace.add(buildWarRoomDashboardView(), "WAR_ROOM");
        mainWorkspace.add(buildDemandMatrixView(), "DEMANDS");
        mainWorkspace.add(buildDonorExchangeView(), "DONORS");
        mainWorkspace.add(buildLogisticsFleetView(), "LOGISTICS");
        mainWorkspace.add(buildInventoryLedgerView(), "INVENTORY");

        add(mainWorkspace, BorderLayout.CENTER);

        // 3. Bottom Tactical Status Ticker
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(TacticalTheme.PANEL_SURFACE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TacticalTheme.BORDER_DIM),
            new EmptyBorder(6, 16, 6, 16)
        ));

        JLabel lblFeed = new JLabel("● DISASTER MONITOR: Heavy runoff detected along Vythiri - Meppadi corridor. Emergency routes active.");
        lblFeed.setFont(TacticalTheme.FONT_MONO_SMALL);
        lblFeed.setForeground(TacticalTheme.ACCENT_AMBER);

        lblClock = new JLabel();
        lblClock.setFont(TacticalTheme.FONT_MONO_SMALL);
        lblClock.setForeground(TacticalTheme.TEXT_SECONDARY);

        Timer clockTimer = new Timer(1000, e -> {
            lblClock.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
        });
        clockTimer.start();

        bottomBar.add(lblFeed, BorderLayout.WEST);
        bottomBar.add(lblClock, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // Top Header with Tactical Branding & Minimal Horizontal Pills
    private JPanel createTopCommandHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TacticalTheme.PANEL_SURFACE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TacticalTheme.BORDER_DIM));

        // Brand Banner
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        brandPanel.setOpaque(false);
        brandPanel.add(new HexRadarBadge(38));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("KERALA STATE DISASTER MANAGEMENT COMMAND");
        title.setFont(TacticalTheme.FONT_TITLE);
        title.setForeground(TacticalTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("TACTICAL AIR-GAP DISPATCH & RELIEF LEDGER // DEPT OF REVENUE");
        subtitle.setFont(TacticalTheme.FONT_MONO_SMALL);
        subtitle.setForeground(TacticalTheme.ACCENT_CYAN);

        textStack.add(title);
        textStack.add(subtitle);
        brandPanel.add(textStack);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Navigation Ribbon
        JPanel navStrip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
        navStrip.setOpaque(false);

        navStrip.add(createNavPill("Tactical Overview", "WAR_ROOM", true));
        navStrip.add(createNavPill("Demand Matrix", "DEMANDS", false));
        navStrip.add(createNavPill("Donor Exchange", "DONORS", false));
        navStrip.add(createNavPill("Logistics Fleet", "LOGISTICS", false));
        navStrip.add(createNavPill("Inventory Ledger", "INVENTORY", false));

        headerPanel.add(navStrip, BorderLayout.EAST);
        return headerPanel;
    }

    private JButton createNavPill(String label, String cardKey, boolean active) {
        JButton btn = new JButton(label);
        btn.setFont(TacticalTheme.FONT_BOLD);
        btn.setForeground(active ? TacticalTheme.BG_VOID : TacticalTheme.TEXT_PRIMARY);
        btn.setBackground(active ? TacticalTheme.ACCENT_EMERALD : TacticalTheme.PANEL_ELEVATED);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? TacticalTheme.ACCENT_EMERALD : TacticalTheme.BORDER_DIM, 1),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            mainCardLayout.show(mainWorkspace, cardKey);
            for (JButton b : navPills) {
                b.setBackground(TacticalTheme.PANEL_ELEVATED);
                b.setForeground(TacticalTheme.TEXT_PRIMARY);
                b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(TacticalTheme.BORDER_DIM, 1),
                    new EmptyBorder(6, 14, 6, 14)
                ));
            }
            btn.setBackground(TacticalTheme.ACCENT_EMERALD);
            btn.setForeground(TacticalTheme.BG_VOID);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TacticalTheme.ACCENT_EMERALD, 1),
                new EmptyBorder(6, 14, 6, 14)
            ));
        });

        navPills.add(btn);
        return btn;
    }

    // =========================================================================
    // VIEW 1: WAR ROOM / TACTICAL OVERVIEW
    // =========================================================================
    private JPanel buildWarRoomDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Center: Interactive Geospatial Canvas
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(TacticalTheme.PANEL_SURFACE);
        mapContainer.setBorder(TacticalTheme.createPanelBorder());

        JPanel mapBar = new JPanel(new BorderLayout());
        mapBar.setOpaque(false);
        JLabel mapTitle = new JLabel("GEOSPATIAL RELIEF NETWORK // KERALA OPERATIONAL CORRIDOR");
        mapTitle.setFont(TacticalTheme.FONT_BOLD);
        mapTitle.setForeground(TacticalTheme.TEXT_PRIMARY);

        JLabel liveStatus = new JLabel("● SYSTEM ACTIVE • LINK SAT-04");
        liveStatus.setFont(TacticalTheme.FONT_MONO_SMALL);
        liveStatus.setForeground(TacticalTheme.ACCENT_EMERALD);

        mapBar.add(mapTitle, BorderLayout.WEST);
        mapBar.add(liveStatus, BorderLayout.EAST);
        mapContainer.add(mapBar, BorderLayout.NORTH);
        mapContainer.add(new TacticalKeralaMapCanvas(), BorderLayout.CENTER);

        panel.add(mapContainer, BorderLayout.CENTER);

        // Right Flank: Telemetry, Gauge & Urgent Queue
        JPanel rightFlank = new JPanel(new GridLayout(3, 1, 0, 12));
        rightFlank.setPreferredSize(new Dimension(340, 0));
        rightFlank.setOpaque(false);

        // Metric Card: Total Dispatched
        rightFlank.add(buildTelemetryCard("STATE LOGISTICS CARGO DISPATCHED", "14,820", "METRIC TONNES / UNITS", TacticalTheme.ACCENT_CYAN));

        // Metric Card: State Fulfillment Donut Arc
        JPanel gaugeCard = new JPanel(new BorderLayout());
        gaugeCard.setBackground(TacticalTheme.PANEL_SURFACE);
        gaugeCard.setBorder(TacticalTheme.createPanelBorder());
        JLabel gTitle = new JLabel("SUPPLY FULFILLMENT INDEX");
        gTitle.setFont(TacticalTheme.FONT_BOLD);
        gTitle.setForeground(TacticalTheme.TEXT_PRIMARY);
        gaugeCard.add(gTitle, BorderLayout.NORTH);
        gaugeCard.add(new TacticalArcGauge(), BorderLayout.CENTER);
        rightFlank.add(gaugeCard);

        // Metric Card: Red Alerts
        rightFlank.add(buildTelemetryCard("CRITICAL SHORTAGE ALERTS", "04", "HIGH RISK EMERGENCY REGIONS", TacticalTheme.ACCENT_CRIMSON));

        panel.add(rightFlank, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildTelemetryCard(String title, String val, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(TacticalTheme.PANEL_SURFACE);
        card.setBorder(TacticalTheme.createPanelBorder());

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(TacticalTheme.FONT_MONO_SMALL);
        lblTitle.setForeground(TacticalTheme.TEXT_SECONDARY);

        JLabel lblVal = new JLabel(val);
        lblVal.setFont(TacticalTheme.FONT_MONO_BIG);
        lblVal.setForeground(accent);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(TacticalTheme.FONT_MONO_SMALL);
        lblSub.setForeground(TacticalTheme.TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // VIEW 2: DEMAND MATRIX VIEW
    // =========================================================================
    private JPanel buildDemandMatrixView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Action Deck (Form Input)
        JPanel inputBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        inputBar.setBackground(TacticalTheme.PANEL_SURFACE);
        inputBar.setBorder(TacticalTheme.createPanelBorder());

        JComboBox<String> cmbCamp = new JComboBox<>(new String[]{"Wayanad (Meppadi Camp)", "Idukki (Chinnakanal Hall)", "Thrissur (Town Hall)", "Alappuzha Relief Base"});
        JTextField txtItem = new JTextField(12);
        JComboBox<String> cmbCat = new JComboBox<>(new String[]{"Medical", "Food", "Drinking Water", "Tarpaulins/Shelter"});
        JTextField txtQty = new JTextField(6);
        JCheckBox chkEscalation = new JCheckBox("Red Alert Priority Multiplier");
        chkEscalation.setOpaque(false);
        chkEscalation.setForeground(TacticalTheme.ACCENT_CRIMSON);
        chkEscalation.setFont(TacticalTheme.FONT_BOLD);

        JButton btnRegister = new JButton("+ Inject Demand");
        btnRegister.setBackground(TacticalTheme.ACCENT_EMERALD);
        btnRegister.setForeground(TacticalTheme.BG_VOID);
        btnRegister.setFont(TacticalTheme.FONT_BOLD);
        btnRegister.setFocusPainted(false);

        inputBar.add(buildFormLabel("Target Sector:"));
        inputBar.add(cmbCamp);
        inputBar.add(buildFormLabel("Resource Name:"));
        inputBar.add(txtItem);
        inputBar.add(buildFormLabel("Category:"));
        inputBar.add(cmbCat);
        inputBar.add(buildFormLabel("Deficit Qty:"));
        inputBar.add(txtQty);
        inputBar.add(chkEscalation);
        inputBar.add(btnRegister);

        panel.add(inputBar, BorderLayout.NORTH);

        // High Density Table
        DefaultTableModel model = new DefaultTableModel(new String[]{"RECORD ID", "SECTOR LOCATION", "RESOURCE SPECIFICATION", "CLASS", "DEMAND", "ALLOCATED", "DYNAMIC SCORE"}, 0);
        model.addRow(new Object[]{"REQ-101", "Wayanad (Meppadi)", "Anti-Venom Vials", "Medical", 150, 30, "24.80"});
        model.addRow(new Object[]{"REQ-102", "Idukki (Chinnakanal)", "High-Energy Ration Packs", "Food", 500, 100, "18.20"});
        model.addRow(new Object[]{"REQ-103", "Alappuzha (Kuttanad)", "Chlorine Tablets & Cans", "Water", 1200, 400, "16.40"});

        panel.add(new JScrollPane(createTacticalTable(model)), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // VIEW 3: DONOR EXCHANGE VIEW
    // =========================================================================
    private JPanel buildDonorExchangeView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel pledgeBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pledgeBar.setBackground(TacticalTheme.PANEL_SURFACE);
        pledgeBar.setBorder(TacticalTheme.createPanelBorder());

        JTextField txtRecordId = new JTextField(8);
        JTextField txtPledgeCount = new JTextField(8);
        JButton btnPledge = new JButton("Confirm Allocation Pledge");
        btnPledge.setBackground(TacticalTheme.ACCENT_CYAN);
        btnPledge.setForeground(TacticalTheme.BG_VOID);
        btnPledge.setFont(TacticalTheme.FONT_BOLD);
        btnPledge.setFocusPainted(false);

        pledgeBar.add(buildFormLabel("Target Request ID:"));
        pledgeBar.add(txtRecordId);
        pledgeBar.add(buildFormLabel("Pledge Units:"));
        pledgeBar.add(txtPledgeCount);
        pledgeBar.add(btnPledge);
        panel.add(pledgeBar, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "SECTOR DESTINATION", "RESOURCE", "CLASS", "DEFICIT SHORTFALL", "ALLOCATED", "PRIORITY"}, 0);
        model.addRow(new Object[]{"REQ-101", "Wayanad (Meppadi)", "Anti-Venom Vials", "Medical", 120, 30, "URGENT [RED]"});
        model.addRow(new Object[]{"REQ-102", "Idukki (Chinnakanal)", "High-Energy Ration Packs", "Food", 400, 100, "ELEVATED [AMBER]"});
        panel.add(new JScrollPane(createTacticalTable(model)), BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // VIEW 4: LOGISTICS FLEET DISPATCH
    // =========================================================================
    private JPanel buildLogisticsFleetView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel formCard = new JPanel(new GridLayout(6, 2, 10, 10));
        formCard.setBackground(TacticalTheme.PANEL_SURFACE);
        formCard.setBorder(TacticalTheme.createPanelBorder());

        JTextField txtDriver = new JTextField("Anish Kumar");
        JTextField txtVehicle = new JTextField("KL-11-BH-4020");
        JTextField txtItemId = new JTextField("REQ-101");
        JTextField txtQty = new JTextField("50");
        JComboBox<String> cmbDest = new JComboBox<>(new String[]{"Wayanad (Meppadi Camp)", "Idukki High School Camp", "Thrissur Relief Center"});

        JButton btnPrintPass = new JButton("Execute Dispatch & Export Security Pass (.txt)");
        btnPrintPass.setBackground(TacticalTheme.ACCENT_AMBER);
        btnPrintPass.setForeground(TacticalTheme.BG_VOID);
        btnPrintPass.setFont(TacticalTheme.FONT_BOLD);

        formCard.add(buildFormLabel("Logistics Pilot / Driver:"));
        formCard.add(txtDriver);
        formCard.add(buildFormLabel("Vehicle Registration:"));
        formCard.add(txtVehicle);
        formCard.add(buildFormLabel("Resource Item Code:"));
        formCard.add(txtItemId);
        formCard.add(buildFormLabel("Quantity Loading:"));
        formCard.add(txtQty);
        formCard.add(buildFormLabel("Destination Outpost:"));
        formCard.add(cmbDest);
        formCard.add(new JLabel(""));
        formCard.add(btnPrintPass);

        panel.add(formCard, BorderLayout.NORTH);
        return panel;
    }

    // =========================================================================
    // VIEW 5: INVENTORY LEDGER
    // =========================================================================
    private JPanel buildInventoryLedgerView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        DefaultTableModel model = new DefaultTableModel(new String[]{"SKU CODE", "ITEM DESCRIPTION", "STORAGE HUB", "GROSS DEMAND", "FULFILLED", "DEFICIT", "STATUS"}, 0);
        model.addRow(new Object[]{"MED-8801", "ORS Sachets & Pediatric Kits", "Kochi Central Hub", 4000, 3100, 900, "NOMINAL"});
        model.addRow(new Object[]{"WAT-3021", "20L Water Containers", "Trivandrum Depot", 8000, 7200, 800, "STABLE"});
        model.addRow(new Object[]{"MED-9904", "Snakebite Anti-Venom", "Kozhikode Depot", 300, 60, 240, "CRITICAL DEFICIT"});

        panel.add(new JScrollPane(createTacticalTable(model)), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // UI REUSABLE ATOMS
    // =========================================================================
    private JLabel buildFormLabel(String txt) {
        JLabel lbl = new JLabel(txt);
        lbl.setFont(TacticalTheme.FONT_BOLD);
        lbl.setForeground(TacticalTheme.TEXT_SECONDARY);
        return lbl;
    }

    private JTable createTacticalTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(TacticalTheme.PANEL_SURFACE);
        table.setForeground(TacticalTheme.TEXT_PRIMARY);
        table.setRowHeight(28);
        table.setFont(TacticalTheme.FONT_MONO_SMALL);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = table.getTableHeader();
        th.setBackground(TacticalTheme.PANEL_ELEVATED);
        th.setForeground(TacticalTheme.ACCENT_CYAN);
        th.setFont(TacticalTheme.FONT_BOLD);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TacticalTheme.BORDER_DIM));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        return table;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KeralaDisasterCommandHQ().setVisible(true);
        });
    }
}