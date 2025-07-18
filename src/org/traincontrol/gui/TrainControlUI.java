package org.traincontrol.gui;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.traincontrol.automation.Edge;
import org.traincontrol.automation.Layout;
import org.traincontrol.automation.Point;
import org.traincontrol.automation.TimetablePath;
import org.traincontrol.base.Locomotive;
import org.traincontrol.marklin.MarklinControlStation;
import org.traincontrol.marklin.MarklinLocomotive;
import org.traincontrol.marklin.MarklinLocomotive.decoderType;
import org.traincontrol.marklin.MarklinRoute;
import org.traincontrol.model.View;
import org.traincontrol.model.ViewListener;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.json.JSONObject;
import org.traincontrol.base.Accessory;
import org.traincontrol.base.Route;
import org.traincontrol.marklin.MarklinAccessory;
import org.traincontrol.marklin.MarklinLayout;
import org.traincontrol.util.Conversion;
import org.traincontrol.util.ImageUtil;
import org.traincontrol.util.Util;

/**
 * UI for controlling trains and switches using the keyboard
 */
public class TrainControlUI extends PositionAwareJFrame implements View 
{    
    // Data save file name
    private static final String DATA_FILE_NAME = "UIState.data";
    public static final String AUTONOMY_FILE_NAME = "autonomy.json";
    
    // External resources
    public static final String DIAGRAM_EDITOR_EXECUTABLE = "TrackDiagramEditor.exe";
    public static final String DIAGRAM_EDITOR_EXECUTABLE_ZIP = "TrackDiagramEditor.zip";
    public static final String DEMO_LAYOUT_ZIP = "sample_layout.zip";
    public static final String GRAPH_CSS_FILE = "graph.css";
    public static final String AUTONOMY_BLANK = "sample_autonomy_blank.json";
    public static final String AUTONOMY_SAMPLE = "sample_autonomy.json";
    public static final String RESOURCE_PATH = "resources/";
    public static final String DEMO_LAYOUT_OUTPUT_PATH = "sample_layout/";
    
    // Tab icons
    public static final Icon TAB_ICON_CONTROL = getTabIcon(30, "tabs/loc.png");
    public static final Icon TAB_ICON_KEYBOARD = getTabIcon(30, "tabs/signal.png");
    public static final Icon TAB_ICON_STATS = getTabIcon(30, "tabs/stats.png");
    public static final Icon TAB_ICON_LAYOUT = getTabIcon(30, "tabs/track.png");
    public static final Icon TAB_ICON_ROUTES = getTabIcon(30, "tabs/route.png");
    public static final Icon TAB_ICON_AUTONOMY = getTabIcon(30, "tabs/autonomy.png");
    public static final Icon TAB_ICON_LOG = getTabIcon(30, "tabs/log.png");
    
    // Repo info
    public static final String GITHUB_REPO = "bob123456678/TrainControl";
    public static final String README_URL = "https://github.com/bob123456678/TrainControl/blob/master/Automation.md";
    public static String UPDATE_URL = "https://github.com/bob123456678/TrainControl/releases";
    public static String LATEST_VERSION = "";
    public static String LATEST_DOWNLOAD_URL = "";
    
    // Preferences fields
    public static final String IP_PREF = "initIP" + Conversion.getFolderHash(10);
    public static final String LAYOUT_OVERRIDE_PATH_PREF = "LayoutOverridePath" + Conversion.getFolderHash(10);
    public static final String SLIDER_SETTING_PREF = "SliderSetting";
    public static final String ROUTE_SORT_PREF = "RouteSorting";
    public static final String ONTOP_SETTING_PREF = "OnTop" + Conversion.getFolderHash(10);
    public static final String MENUBAR_SETTING_PREF = "MenuBarOn";
    public static final String AUTOSAVE_SETTING_PREF = "AutoSave";
    public static final String HIDE_REVERSING_PREF = "HideReversing";
    public static final String HIDE_INACTIVE_PREF = "HideInactive";
    public static final String SHOW_STATION_LENGTH = "ShowStationLength";
    public static final String LAST_USED_FOLDER = "LastUsedFolder";
    public static final String LAST_USED_ICON_FOLDER = "LastUsedIconFolder";
    public static final String KEYBOARD_LAYOUT = "KeyboardLayout";
    public static final String SHOW_KEYBOARD_HINTS_PREF = "KeyboardHits";
    public static final String ACTIVE_LOC_IN_TITLE = "ActiveLocInTitle";
    public static final String CHECK_FOR_UPDATES = "CheckForUpdates";
    public static final String AUTO_POWER_ON = "AutoPowerOn" + Conversion.getFolderHash(10);
    public static final String LAYOUT_TITLES_PREF = "LayoutTitlesPref";
    public static final String AUTO_LOAD_AUTONOMY = "AutoLoadAutonomy" + Conversion.getFolderHash(10);
    public static final String PREFERRED_KEYBOARD_MM2 = "PreferredKeyboardMM2";
    public static final String LAYOUT_SHOW_ADDRESSES = "LayoutShowAddresses";
    
    // Preference defaults
    public static final boolean ONTOP_SETTING_DEFAULT = true; // This is needed because this setting is read at startup

    // Message strings
    public static final String NO_LOC_MESSAGE = "There are no locomotives currently in the database. Add some in the Locomotives menu, or via the Central Station, and then synchronize.";
    
    // Keyboard layout constants
    public static final String KEYBOARD_QWERTY = "QWERTY";
    public static final String KEYBOARD_QWERTZ = "QWERTZ";
    public static final String KEYBOARD_AZERTY = "AZERTY";

    public static final String[] KEYBOARD_TYPES = {KEYBOARD_QWERTY, KEYBOARD_QWERTZ, KEYBOARD_AZERTY};
    
    // Adjustable constants /////////////
    
    // Width of locomotive images
    public static final Integer LOC_ICON_WIDTH = 296;
    
    // Max height of locomotive images
    public static final Integer LOC_ICON_HEIGHT = 114;
    
    // Width of button images
    public static final Integer BUTTON_ICON_WIDTH = 34;
    
    // Maximum allowed length of locomotive names and notes (in characters)
    public static final Integer MAX_LOC_NAME_DATABASE = 30;
    public static final Integer MAX_LOC_NOTES_LENGTH = 2000;

    // Maximum page name length
    public static final Integer MAX_PAGE_NAME_LENGTH = 40;
    public static final Integer MAX_PAGE_NAME_LENGTH_TOP = 10;

    // Maximum displayed locomotive name length
    public static final Integer MAX_LOC_NAME = 30;
    public static final Integer MAX_MU_SELECTOR_LOC_NAME_LENGTH = 16;
    
    // Minimum time between possible route repainting in semi-autonmous mode
    public static final Integer REPAINT_ROUTE_INTERVAL = 100;
    
    // How often we measure latency. 0 to disable
    public static final Integer PING_INTERVAL = 5000;
    
    // Thresholds at which high ping is highlighted
    public static final Integer PING_ORANGE = 100;
    public static final Integer PING_RED = 300;

    // Do we load any images?
    public static final boolean LOAD_IMAGES = true;
    
    // How much to increment speed when the arrow keys are pressed
    public static final int SPEED_STEP = 4;
    
    // Number of seconds to wait before checking for CAN activity
    private static final int CAN_MONITOR_DELAY = 15;
    
    // Total number of keyboards >= 1
    private static final int NUM_KEYBOARDS = 32;
    
    // Total number of locomotive mappings >= 1
    private static final int NUM_LOC_MAPPINGS = 10;
    
    // How many columns to show in the route UI
    private static final int ROUTE_UI_COLS = 3;
    
    // Keyboard colors
    private static final Color COLOR_SWITCH_RED = new Color(255, 204, 204);
    private static final Color COLOR_SWITCH_GREEN = new Color(204, 255, 204);
    
    // Internal fields /////////////
    
    // View listener (model) reference
    private ViewListener model;
    
    // Graph viewer instance
    private GraphViewer graphViewer;
    
    // The active locomotive
    private MarklinLocomotive activeLoc;
    
    // The active locomotive button
    private javax.swing.JButton currentButton;
    
    // Page where the current button was selected
    private int currentButtonlocMappingNumber;
    
    private final HashMap<Integer, javax.swing.JButton> buttonMapping;
    private final HashMap<javax.swing.JButton, JTextField> labelMapping;
    private final HashMap<javax.swing.JButton, javax.swing.JSlider> sliderMapping;
    private final HashMap<javax.swing.JSlider, javax.swing.JButton> rSliderMapping;
    private final List<HashMap<javax.swing.JButton, Locomotive>> locMapping;
    private final HashMap<javax.swing.JToggleButton, Integer> functionMapping;
    private final HashMap<Integer, javax.swing.JToggleButton> rFunctionMapping;
    private final HashMap<Integer, javax.swing.JToggleButton> switchMapping;
    
    // Custom labels for mapping pages
    private Map<Integer, String> pageNames;

    private LayoutGrid trainGrid;
    private ExecutorService LayoutGridRenderer = Executors.newFixedThreadPool(1);
    private ExecutorService AutonomyRenderer = Executors.newFixedThreadPool(1);
    private ExecutorService MappingRenderer = Executors.newFixedThreadPool(1);
    private ExecutorService LocRenderer = Executors.newFixedThreadPool(1);
    private ExecutorService ImageLoader = Executors.newFixedThreadPool(4);
    private ExecutorService ImageLoaderLoc = Executors.newFixedThreadPool(2);
    private List<Future<?>> autonomyFutures = new LinkedList<>();
    private List<Future<?>> locFutures = new LinkedList<>();

    // The keyboard being displayed
    private int keyboardNumber = 1;
    
    // The locomotive mapping page being displayed
    private int locMappingNumber = 1;
    
    // The locomotive mapping that was just painted
    private int lastLocMappingPainted = 0;
    
    // Number of keys per page
    private static final int KEYBOARD_KEYS = 64;
        
    // Maximum number of functions
    private static final int NUM_FN = 32;
        
    // Hack to store additional information in the mapping data
    private static final int SAVE_KEY_ACTIVE_MAPPING_NUMBER = -1;
    private static final int SAVE_KEY_ACTIVE_BUTTON = -2;
                     
    // Image cache
    private static HashMap<String, Image> imageCache;
    
    // Layout cache (speeds up rendering)
    public HashMap<String, JPanel> layoutCache = new HashMap<>();
    
    // Preferences
    private static final Preferences prefs = Preferences.userNodeForPackage(TrainControlUI.class);
    private boolean conditionalRouteWarningShown = false;
    
    // Locomotive clipboard 
    private Locomotive copyTarget = null;
    private JButton copyTargetButton = null;
    private int copyTargetPage = 0;
    
    // Page clipboard
    private Integer pageToCopy = null;
    
    // Other UIs
    private LocomotiveSelector selector;
    private AddLocomotive adder;
    private LocomotiveStats stats;
    private RouteEditor routeEditor;

    // Popup references
    private List<LayoutPopupUI> popups = new ArrayList<>();
    
    // So we can track locomotive (autonomy) locations on track diagrams
    private final Map<String, Set<JLabel>> layoutStations = new HashMap<>();
         
    // Quick search cache
    private String lastSearch = "";
    private LinkedHashSet<LocomotiveKeyboardMapping> lastResults = new LinkedHashSet<>();
    
    // Cache timetable state to minimize repaining
    private int lastTimetableState = 0;
    
    // Help ensure that duplicate accessory commands are not captured
    private String lastCapturedAccessoryCommand;
    private long lastCapturedAccessoryCommandTime;
    private static final int CAPTURE_COMMAND_THROTTLE = 5000;
        
    /**
     * Creates new form MarklinUI
     */
    public TrainControlUI()
    {
        System.setProperty("org.graphstream.ui", "swing");

        FlatLightLaf.setup();
        //FlatIntelliJLaf.setup();

        // Makes tabs narrower
        javax.swing.UIManager.put("TabbedPane.tabWidthMode", "compact");
        javax.swing.UIManager.put("TabbedPane.tabInsets", new Insets(8, 8, 8, 8));
        
        initComponents();
        
        // Mappings allowing us to programatically access UI components
        this.buttonMapping = new HashMap<>();
        this.labelMapping = new HashMap<>();
        this.switchMapping = new HashMap<>();
        this.functionMapping = new HashMap<>();
        this.rFunctionMapping = new HashMap<>();
        this.sliderMapping = new HashMap<>();
        this.rSliderMapping = new HashMap<>();
        this.pageNames = new HashMap<>();
        
        this.locMapping = new ArrayList<>();
    
        for (int i = 0; i < TrainControlUI.NUM_LOC_MAPPINGS; i++)
        {
            this.locMapping.add(new HashMap<>());
        }

        // Map function buttons to numbers
        this.functionMapping.put(F0, 0);
        this.functionMapping.put(F1, 1);
        this.functionMapping.put(F2, 2);
        this.functionMapping.put(F3, 3);
        this.functionMapping.put(F4, 4);
        this.functionMapping.put(F5, 5);
        this.functionMapping.put(F6, 6);
        this.functionMapping.put(F7, 7);
        this.functionMapping.put(F8, 8);
        this.functionMapping.put(F9, 9);
        this.functionMapping.put(F10, 10);
        this.functionMapping.put(F11, 11);
        this.functionMapping.put(F12, 12);
        this.functionMapping.put(F13, 13);
        this.functionMapping.put(F14, 14);
        this.functionMapping.put(F15, 15);
        this.functionMapping.put(F16, 16);
        this.functionMapping.put(F17, 17);
        this.functionMapping.put(F18, 18);
        this.functionMapping.put(F19, 19);
        this.functionMapping.put(F20, 20);
        this.functionMapping.put(F21, 21);
        this.functionMapping.put(F22, 22);
        this.functionMapping.put(F23, 23);
        this.functionMapping.put(F24, 24);
        this.functionMapping.put(F25, 25);
        this.functionMapping.put(F26, 26);
        this.functionMapping.put(F27, 27);
        this.functionMapping.put(F28, 28);
        this.functionMapping.put(F29, 29);
        this.functionMapping.put(F30, 30);
        this.functionMapping.put(F31, 31);
        
        // Map numbers back to the corresponding buttons
        for (javax.swing.JToggleButton b : this.functionMapping.keySet())
        {
            this.rFunctionMapping.put(this.functionMapping.get(b), b);
        }
        
        // Map keyboard buttons to buttons
        initButtonMapping();
        
        // Speed slider mappings
        this.sliderMapping.put(AButton, ASlider);
        this.sliderMapping.put(BButton, BSlider);
        this.sliderMapping.put(CButton, CSlider);
        this.sliderMapping.put(DButton, DSlider);
        this.sliderMapping.put(EButton, ESlider);
        this.sliderMapping.put(FButton, FSlider);
        this.sliderMapping.put(GButton, GSlider);
        this.sliderMapping.put(HButton, HSlider);
        this.sliderMapping.put(IButton, ISlider);
        this.sliderMapping.put(JButton, JSlider);
        this.sliderMapping.put(KButton, KSlider);
        this.sliderMapping.put(LButton, LSlider);
        this.sliderMapping.put(MButton, MSlider);
        this.sliderMapping.put(NButton, NSlider);
        this.sliderMapping.put(OButton, OSlider);
        this.sliderMapping.put(PButton, PSlider);
        this.sliderMapping.put(QButton, QSlider);
        this.sliderMapping.put(RButton, RSlider);
        this.sliderMapping.put(SButton, SSlider);
        this.sliderMapping.put(TButton, TSlider);
        this.sliderMapping.put(UButton, USlider);
        this.sliderMapping.put(VButton, VSlider);
        this.sliderMapping.put(WButton, WSlider);
        this.sliderMapping.put(XButton, XSlider);
        this.sliderMapping.put(YButton, YSlider);
        this.sliderMapping.put(ZButton, ZSlider);

        this.rSliderMapping.put(ASlider, AButton);
        this.rSliderMapping.put(BSlider, BButton);
        this.rSliderMapping.put(CSlider, CButton);
        this.rSliderMapping.put(DSlider, DButton);
        this.rSliderMapping.put(ESlider, EButton);
        this.rSliderMapping.put(FSlider, FButton);
        this.rSliderMapping.put(GSlider, GButton);
        this.rSliderMapping.put(HSlider, HButton);
        this.rSliderMapping.put(ISlider, IButton);
        this.rSliderMapping.put(JSlider, JButton);
        this.rSliderMapping.put(KSlider, KButton);
        this.rSliderMapping.put(LSlider, LButton);
        this.rSliderMapping.put(MSlider, MButton);
        this.rSliderMapping.put(NSlider, NButton);
        this.rSliderMapping.put(OSlider, OButton);
        this.rSliderMapping.put(PSlider, PButton);
        this.rSliderMapping.put(QSlider, QButton);
        this.rSliderMapping.put(RSlider, RButton);
        this.rSliderMapping.put(SSlider, SButton);
        this.rSliderMapping.put(TSlider, TButton);
        this.rSliderMapping.put(USlider, UButton);
        this.rSliderMapping.put(VSlider, VButton);
        this.rSliderMapping.put(WSlider, WButton);
        this.rSliderMapping.put(XSlider, XButton);
        this.rSliderMapping.put(YSlider, YButton);
        this.rSliderMapping.put(ZSlider, ZButton);
        
        // Map letters back to the corresponding buttons
        for (Entry<Integer, JButton> entry : this.buttonMapping.entrySet())
        {            
            // Add right click events
            this.buttonMapping.get(entry.getKey()).addMouseListener(new RightClickMenuListener(this, entry.getValue()));
        }
        
        // Map buttons to labels
        this.labelMapping.put(AButton, ALabel);
        this.labelMapping.put(BButton, BLabel);
        this.labelMapping.put(CButton, CLabel);
        this.labelMapping.put(DButton, DLabel);
        this.labelMapping.put(EButton, ELabel);
        this.labelMapping.put(FButton, FLabel);
        this.labelMapping.put(GButton, GLabel);
        this.labelMapping.put(HButton, HLabel);
        this.labelMapping.put(IButton, ILabel);
        this.labelMapping.put(JButton, JLabel);
        this.labelMapping.put(KButton, KLabel);
        this.labelMapping.put(LButton, LLabel);
        this.labelMapping.put(MButton, MLabel);
        this.labelMapping.put(NButton, NLabel);
        this.labelMapping.put(OButton, OLabel);
        this.labelMapping.put(PButton, PLabel);
        this.labelMapping.put(QButton, QLabel);
        this.labelMapping.put(RButton, RLabel);
        this.labelMapping.put(SButton, SLabel);
        this.labelMapping.put(TButton, TLabel);
        this.labelMapping.put(UButton, ULabel);
        this.labelMapping.put(VButton, VLabel);
        this.labelMapping.put(WButton, WLabel);
        this.labelMapping.put(XButton, XLabel);
        this.labelMapping.put(YButton, YLabel);
        this.labelMapping.put(ZButton, ZLabel);
        
        // Map switch addresses to buttons
        this.switchMapping.put(1,SwitchButton1);
        this.switchMapping.put(2,SwitchButton2);
        this.switchMapping.put(3,SwitchButton3);
        this.switchMapping.put(4,SwitchButton4);
        this.switchMapping.put(5,SwitchButton5);
        this.switchMapping.put(6,SwitchButton6);
        this.switchMapping.put(7,SwitchButton7);
        this.switchMapping.put(8,SwitchButton8);
        this.switchMapping.put(9,SwitchButton9);
        this.switchMapping.put(10,SwitchButton10);
        this.switchMapping.put(11,SwitchButton11);
        this.switchMapping.put(12,SwitchButton12);
        this.switchMapping.put(13,SwitchButton13);
        this.switchMapping.put(14,SwitchButton14);
        this.switchMapping.put(15,SwitchButton15);
        this.switchMapping.put(16,SwitchButton16);
        this.switchMapping.put(17,SwitchButton17);
        this.switchMapping.put(18,SwitchButton18);
        this.switchMapping.put(19,SwitchButton19);
        this.switchMapping.put(20,SwitchButton20);
        this.switchMapping.put(21,SwitchButton21);
        this.switchMapping.put(22,SwitchButton22);
        this.switchMapping.put(23,SwitchButton23);
        this.switchMapping.put(24,SwitchButton24);
        this.switchMapping.put(25,SwitchButton25);
        this.switchMapping.put(26,SwitchButton26);
        this.switchMapping.put(27,SwitchButton27);
        this.switchMapping.put(28,SwitchButton28);
        this.switchMapping.put(29,SwitchButton29);
        this.switchMapping.put(30,SwitchButton30);
        this.switchMapping.put(31,SwitchButton31);
        this.switchMapping.put(32,SwitchButton32);
        this.switchMapping.put(33,SwitchButton33);
        this.switchMapping.put(34,SwitchButton34);
        this.switchMapping.put(35,SwitchButton35);
        this.switchMapping.put(36,SwitchButton36);
        this.switchMapping.put(37,SwitchButton37);
        this.switchMapping.put(38,SwitchButton38);
        this.switchMapping.put(39,SwitchButton39);
        this.switchMapping.put(40,SwitchButton40);
        this.switchMapping.put(41,SwitchButton41);
        this.switchMapping.put(42,SwitchButton42);
        this.switchMapping.put(43,SwitchButton43);
        this.switchMapping.put(44,SwitchButton44);
        this.switchMapping.put(45,SwitchButton45);
        this.switchMapping.put(46,SwitchButton46);
        this.switchMapping.put(47,SwitchButton47);
        this.switchMapping.put(48,SwitchButton48);
        this.switchMapping.put(49,SwitchButton49);
        this.switchMapping.put(50,SwitchButton50);
        this.switchMapping.put(51,SwitchButton51);
        this.switchMapping.put(52,SwitchButton52);
        this.switchMapping.put(53,SwitchButton53);
        this.switchMapping.put(54,SwitchButton54);
        this.switchMapping.put(55,SwitchButton55);
        this.switchMapping.put(56,SwitchButton56);
        this.switchMapping.put(57,SwitchButton57);
        this.switchMapping.put(58,SwitchButton58);
        this.switchMapping.put(59,SwitchButton59);
        this.switchMapping.put(60,SwitchButton60);
        this.switchMapping.put(61,SwitchButton61);
        this.switchMapping.put(62,SwitchButton62);
        this.switchMapping.put(63,SwitchButton63);
        this.switchMapping.put(64,SwitchButton64);
        
        // Load actuation count dynamically into tooltip
        for (JToggleButton j : this.switchMapping.values())
        {
            j.addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseMoved(MouseEvent e)
                {    
                    int accAddress = Integer.parseInt(((JToggleButton) e.getSource()).getText());
                    
                    ((JToggleButton) e.getSource()).setToolTipText(
                            model.getAccessoryByAddress(accAddress, getKeyboardProtocol()).getName() + " actuation count: " + 
                                    model.getAccessoryByAddress(accAddress, getKeyboardProtocol()).getNumActuations()   
                    );
                }
            });
        }
        
        // Prevent the tabbed pane from being stupid
        this.KeyboardTab.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke("LEFT"), "none");
        this.KeyboardTab.getInputMap(JComponent.WHEN_FOCUSED)
            .put(KeyStroke.getKeyStroke("RIGHT"), "none");
           
        // Restore UI component state
        this.slidersChangeActiveLocMenuItem.setSelected(prefs.getBoolean(SLIDER_SETTING_PREF, false));
        this.showKeyboardHintsMenuItem.setSelected(prefs.getBoolean(SHOW_KEYBOARD_HINTS_PREF, true));
        this.windowAlwaysOnTopMenuItem.setSelected(prefs.getBoolean(ONTOP_SETTING_PREF, ONTOP_SETTING_DEFAULT));
        this.toggleMenuBar.setSelected(prefs.getBoolean(MENUBAR_SETTING_PREF, true));
        this.autosave.setSelected(prefs.getBoolean(AUTOSAVE_SETTING_PREF, true));
        this.hideReversing.setSelected(prefs.getBoolean(HIDE_REVERSING_PREF, false));
        this.hideInactive.setSelected(prefs.getBoolean(HIDE_INACTIVE_PREF, false));
        this.showStationLengths.setSelected(prefs.getBoolean(SHOW_STATION_LENGTH, true));
        this.activeLocInTitle.setSelected(prefs.getBoolean(ACTIVE_LOC_IN_TITLE, true));
        this.checkForUpdates.setSelected(prefs.getBoolean(CHECK_FOR_UPDATES, true));
        this.AutoLoadAutonomyMenuItem.setSelected(prefs.getBoolean(AUTO_LOAD_AUTONOMY, false));
        this.menuItemShowLayoutAddresses.setSelected(prefs.getBoolean(LAYOUT_SHOW_ADDRESSES, false));
        
        if (prefs.getBoolean(PREFERRED_KEYBOARD_MM2, true))
        {
            this.MM2.setSelected(true);
        }
        else
        {
            this.DCC.setSelected(true);
        }
        
        // Set selected route sort radio button
        this.sortByID.setSelected(!prefs.getBoolean(ROUTE_SORT_PREF, false));
        this.sortByName.setSelected(prefs.getBoolean(ROUTE_SORT_PREF, false));
        
        // Right-clicks on the route list
        this.RouteList.addMouseListener(new RightClickRouteMenu(this)); 
        
        // Right-clicks on the timetable
        this.timetable.addMouseListener(new RightClickTimetableMenu(this));  
        
        // Power at startup preference
        switch (prefs.getInt(TrainControlUI.AUTO_POWER_ON, 0))
        {
            case 0:
                this.powerOnStartup.setSelected(true);
                break;
            case 1:
                this.powerOffStartup.setSelected(true);
                break;
            case 2:
                this.powerNoChangeStartup.setSelected(true);
                break;
        }
        
        // Keyboard layout preference
        switch (prefs.getInt(TrainControlUI.KEYBOARD_LAYOUT, 0))
        {
            case 0:
                this.keyboardQwertyMenuItem.setSelected(true);
                break;
            case 1:
                this.keyboardQwertzMenuItem.setSelected(true);
                break;
            case 2:
                this.keyboardAzertyMenuItem.setSelected(true);
                break;
        }
        
        // Window location preference
        this.rememberLocationMenuItem.setSelected(prefs.getBoolean(REMEMBER_WINDOW_LOCATION, false));
        
        this.applyKeyboardType(TrainControlUI.KEYBOARD_TYPES[getSelectedKeyboardType() >= 0 ? getSelectedKeyboardType() : 0]);
        
        // Support for changing page names
        RightClickPageMenu rcm = new RightClickPageMenu(this);
        this.LocMappingNumberLabel.addMouseListener(rcm);
        this.PrevLocMapping.addMouseListener(rcm);
        this.NextLocMapping.addMouseListener(rcm);
        
        displayKeyboardHints(prefs.getBoolean(SHOW_KEYBOARD_HINTS_PREF, true)); 
        
        // Style tables
        timetable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
    
    /**
     * Registers a track diagram label as corresponding to an autonomy station (point)
     * @param key
     * @param value 
     */
    public void addLayoutStation(String key, JLabel value)
    {
        // Use computeIfAbsent to initialize the set if it doesn't exist
        layoutStations.computeIfAbsent(key, k -> new HashSet<>()).add(value);
    }
    
    /**
     * Get all labels corresponding to a station (point)
     * @param key
     * @return 
     */
    public Set<JLabel> getLayoutStations(String key)
    {
        return layoutStations.getOrDefault(key, Collections.emptySet());
    }
    
    /**
     * Clears out the labels, i.e. after reloading autonomy
     */
    public void resetLayoutStationLabels()
    {
        if (this.model.hasAutoLayout() && !this.model.getAutoLayout().isRunning())
        {
            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
            {  
                for (Set<JLabel> labelSet : layoutStations.values())
                {
                    for (JLabel j : labelSet)
                    {
                        j.setText("");
                        j.setOpaque(false);
                    }
                }
            }));
        }
    }
    
    /**
     * Gets the keyboard type preference from the radio button group
     * @return 
     */
    private int getSelectedKeyboardType()
    {        
        try 
        {
            return Integer.parseInt(this.buttonGroup3.getSelection().getActionCommand());
        }
        catch (NumberFormatException e)
        {
            return -1;
        }  
    }
    
    /**
     * Switches to a different keyboard layout
     * @param type 
     */
    private void applyKeyboardType(String type)
    {            
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {    
            initButtonMapping();

            switch (type)
            {
                case KEYBOARD_QWERTY:

                    // Already handled by the call above

                    break;

                case KEYBOARD_AZERTY:

                    this.swapButton(KeyEvent.VK_A, KeyEvent.VK_Q);
                    this.swapButton(KeyEvent.VK_Z, KeyEvent.VK_W);

                    break;

                case KEYBOARD_QWERTZ:

                    this.swapButton(KeyEvent.VK_Z, KeyEvent.VK_Y);

                    break;
            }
                          
            // Repaint to fix shadow bug - wouldn't be changed by repaintMappings b/c we are only changing the button labels
            this.repaintIcon(AButton, this.getButtonLocomotive(AButton), this.locMappingNumber);
            this.repaintIcon(ZButton, this.getButtonLocomotive(ZButton), this.locMappingNumber);
            this.repaintIcon(WButton, this.getButtonLocomotive(WButton), this.locMappingNumber);
            this.repaintIcon(YButton, this.getButtonLocomotive(YButton), this.locMappingNumber);
            this.repaintIcon(QButton, this.getButtonLocomotive(QButton), this.locMappingNumber);

            // Set text labels - letter of the mapping
            for (Entry<Integer, JButton> entry : this.buttonMapping.entrySet())
            {
                entry.getValue().setText(KeyEvent.getKeyText(entry.getKey()).toUpperCase());
            }

            this.repaintMappings();
        }));
    }
       
    /**
     * Utility function to facilitate button swapping
     * @param key1
     * @param key2 
     */
    private void swapButton(int key1, int key2)
    {
        JButton firstButton = this.buttonMapping.get(key1);
        JButton secondButton = this.buttonMapping.get(key2);
        
        String firstButtonLabel = firstButton.getText();
        String secondButtonLabel = secondButton.getText();
        
        firstButton.setText(secondButtonLabel);
        secondButton.setText(firstButtonLabel);
                
        this.buttonMapping.put(key1, secondButton);
        this.buttonMapping.put(key2, firstButton);
    }
    
    /**
     * Maps keystrokes to buttons
     */
    private void initButtonMapping()
    {
        this.buttonMapping.put(KeyEvent.VK_A, AButton);
        this.buttonMapping.put(KeyEvent.VK_B, BButton);
        this.buttonMapping.put(KeyEvent.VK_C, CButton);
        this.buttonMapping.put(KeyEvent.VK_D, DButton);
        this.buttonMapping.put(KeyEvent.VK_E, EButton);
        this.buttonMapping.put(KeyEvent.VK_F, FButton);
        this.buttonMapping.put(KeyEvent.VK_G, GButton);
        this.buttonMapping.put(KeyEvent.VK_H, HButton);
        this.buttonMapping.put(KeyEvent.VK_I, IButton);
        this.buttonMapping.put(KeyEvent.VK_J, JButton);
        this.buttonMapping.put(KeyEvent.VK_K, KButton);
        this.buttonMapping.put(KeyEvent.VK_L, LButton);
        this.buttonMapping.put(KeyEvent.VK_M, MButton);
        this.buttonMapping.put(KeyEvent.VK_N, NButton);
        this.buttonMapping.put(KeyEvent.VK_O, OButton);
        this.buttonMapping.put(KeyEvent.VK_P, PButton);
        this.buttonMapping.put(KeyEvent.VK_Q, QButton);
        this.buttonMapping.put(KeyEvent.VK_R, RButton);
        this.buttonMapping.put(KeyEvent.VK_S, SButton);
        this.buttonMapping.put(KeyEvent.VK_T, TButton);
        this.buttonMapping.put(KeyEvent.VK_U, UButton);
        this.buttonMapping.put(KeyEvent.VK_V, VButton);
        this.buttonMapping.put(KeyEvent.VK_W, WButton);
        this.buttonMapping.put(KeyEvent.VK_X, XButton);
        this.buttonMapping.put(KeyEvent.VK_Y, YButton);
        this.buttonMapping.put(KeyEvent.VK_Z, ZButton);
    }
        
    public static Preferences getPrefs()
    {
        return prefs;
    }
    
    /**
     * Returns the key code corresponding to the currently selected locomotive button
     * @return 
     */
    private Integer getKeyForCurrentButton()
    {
        for (Entry<Integer, JButton> entry : this.buttonMapping.entrySet())
        {
            if (entry.getValue().equals(this.currentButton))
            {
                return entry.getKey();
            }
        }
        
        return -1;
    }
    
    /**
     * Saves initialized component database to a file
     * @param backup
     */
    public void saveState(boolean backup)
    {
        String prefix = backup ? ("backup" + Conversion.convertSecondsToDatetime(System.currentTimeMillis()).replace(':', '-').replace(' ', '_')) : "";
        
        List<Map<Integer,String>> l = new ArrayList<>();
        
        for (int i = 0; i < this.locMapping.size(); i++)
        {
            Map<Integer,String> newMap = new HashMap<>();
            
            for (JButton b : this.locMapping.get(i).keySet())
            {
                Locomotive loc = this.locMapping.get(i).get(b);

                if (loc != null)
                {
                    newMap.put(KeyEvent.getExtendedKeyCodeForChar(b.getText().charAt(0)), loc.getName());
                }
            }
            
            l.add(newMap);
        }
                
        // Index -1 is the currently active page, -2 is the currently active button
        this.pageNames.put(SAVE_KEY_ACTIVE_MAPPING_NUMBER, Integer.toString(this.locMappingNumber));
        this.pageNames.put(SAVE_KEY_ACTIVE_BUTTON, Integer.toString(this.getKeyForCurrentButton()));

        // Save page names
        l.add(this.pageNames);
        
        try
        {
            // Write object with ObjectOutputStream to disk using
            // FileOutputStream
            ObjectOutputStream obj_out = new ObjectOutputStream(
                new FileOutputStream(prefix + TrainControlUI.DATA_FILE_NAME));

            // Write object out to disk
            obj_out.writeObject(l);

            this.model.log("Saving UI state to: " + new File(prefix + TrainControlUI.DATA_FILE_NAME).getAbsolutePath());
        } 
        catch (IOException iOException)
        {
            this.model.log("Could not save UI state. " + iOException.getMessage());
        }
        
        if (this.autosave.isSelected() && this.model.hasAutoLayout() 
                && this.model.getAutoLayout().isValid()
                && !this.model.getAutoLayout().getPoints().isEmpty())
        {
            if (this.model.getAutoLayout().isRunning())
            {
                this.model.log("Autonomy still running: skipping JSON auto-save.");
            }
            else
            {
                try
                {
                    this.autonomyJSON.setText(this.getModel().getAutoLayout().toJSON());
                    this.model.log("Auto-saving autonomy state...");
                }
                catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException ex)
                {
                    this.model.log("Failed to save auto layout JSON.");
                }
            }
        }
        
        if (!this.autonomyJSON.getText().trim().equals(""))
        {
            try 
            {
                this.model.log("Saving autonomy JSON to: " + new File(prefix + TrainControlUI.AUTONOMY_FILE_NAME).getAbsolutePath());

                ObjectOutputStream obj_out = new ObjectOutputStream(
                    new FileOutputStream(prefix + TrainControlUI.AUTONOMY_FILE_NAME));

                // Write object out to disk
                obj_out.writeObject(this.autonomyJSON.getText());                
            }
            catch (IOException iOException)
            {
                this.model.log("Could not save autonomy JSON. " 
                    + iOException.getMessage());
            }
        }
        
        saveLayoutTitles();
    }
    
    /**
     * Gets a list of all buttons mapped to the given locomotive
     * @param l
     * @return 
     */
    public List<String> getAllLocButtonMappings(Locomotive l)
    {
        List<String> out = new ArrayList<>();
        
        for (Integer i = 0; i < this.locMapping.size(); i++)
        {
            for (Entry<JButton, Locomotive> entry : this.locMapping.get(i).entrySet())
            {
                if (l != null && l.equals(entry.getValue()))
                {
                    out.add(entry.getKey().getText() + " (Page " + Integer.toString(i + 1) + ")");
                }
            }
        }
        
        return out;
    }
        
    /**
     * Gets a map of all buttons mapped to the given locomotive name, prioritizing exact matches
     * @param s
     * @return 
     */
    public LinkedHashSet<LocomotiveKeyboardMapping> getAllLocButtonMappingsMap(String s)
    {
        LinkedHashSet<LocomotiveKeyboardMapping> out = new LinkedHashSet<>();
        
        if (s != null)
        {
            // Exact matches come first
            for (Integer i = 0; i < this.locMapping.size(); i++)
            {
                for (Entry<JButton, Locomotive> entry : this.locMapping.get(i).entrySet())
                {
                    if (entry.getValue() != null && entry.getValue().getName().trim().toLowerCase().equals(s.trim().toLowerCase()))
                    {
                        out.add(new LocomotiveKeyboardMapping(i + 1, entry.getKey()));
                    }
                }
            }
            
            for (Integer i = 0; i < this.locMapping.size(); i++)
            {
                for (Entry<JButton, Locomotive> entry : this.locMapping.get(i).entrySet())
                {
                    if (entry.getValue() != null && entry.getValue().getName().trim().toLowerCase().contains(s.trim().toLowerCase()))
                    {
                        out.add(new LocomotiveKeyboardMapping(i + 1, entry.getKey()));
                    }
                }
            }
        }
        
        return out;
    }
    
    /**
     * Finds a locomotive matching the passed string, and activates its button
     * Gives priority to exact matches
     * @param s
     */
    public void jumpToLocomotive(String s)
    {        
        // Empty string - repeat last search
        if ("".equals(s))
        {
            s = this.lastSearch;
        }
        
        if ("".equals(s))
        {
            return;
        }
        
        // Maintain history of results
        if (!this.lastSearch.equals(s))
        {
            this.lastSearch = s;
            this.lastResults.clear();
        }
        
        // Get all possible results
        LinkedHashSet<LocomotiveKeyboardMapping> results = this.getAllLocButtonMappingsMap(s);
                
        this.model.log("Found " + s + " mapped at: " + results);

        // Reset and loop around if all results already found
        if (this.lastResults.size() == results.size())
        {
            this.lastResults.clear();
        }
        
        // Show first unseen result
        for (LocomotiveKeyboardMapping e : results)
        {
            if (!this.lastResults.contains(e))
            {
                this.lastResults.add(e);
                
                // Exclude current button
                if (this.locMappingNumber == e.getPage() && this.currentButton.equals(e.getButton())
                        && results.size() - this.lastResults.size() > 1)
                {
                    continue;
                }
                
                // Display it
                if (this.locMappingNumber != e.getPage())
                {
                    this.switchLocMapping(e.getPage());
                }

                this.displayCurrentButtonLoc(e.getButton());
                
                return;
            }
        }
    }

    /**
     * Restores list of initialized components from a file
     * @return 
     */
    public final List<Map<Integer,String>> restoreState()
    {
        List<Map<Integer,String>> instance = new ArrayList<>();
        
        try
        {
            // Read object using ObjectInputStream
            ObjectInputStream obj_in = new ObjectInputStream(
                new FileInputStream(TrainControlUI.DATA_FILE_NAME)
            );
            
            // Read an object
            Object obj = obj_in.readObject();

            if (obj instanceof List)
            {
                // Cast object
                instance = (List<Map<Integer,String>>) obj;
            }

            this.model.log("UI state loaded from file.");
        } 
        catch (IOException iOException)
        {
            this.model.log("No data file found, "
                    + "UI initializing with default data");
        } 
        catch (ClassNotFoundException classNotFoundException)
        {
            this.model.log("Bad data file for UI");            
        }
        
        return instance;
    }
    
    /**
     * Returns a reference to the image cache, initializing it if needed
     * @return 
     */
    synchronized public static Map<String,Image> getImageCache()
    {
        if (imageCache == null)
        {
            imageCache = new HashMap<>();
        }
        
        return imageCache;
    }
    
    /**
     * Logs a message
     * @param message 
     */
    @Override
    public void log(String message)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            this.debugArea.insert(message + "\n", 0);
            this.debugArea.setCaretPosition(0);
        }));
    }             
    
    public int getKeyboardOffset()
    {
        return (this.keyboardNumber - 1) * TrainControlUI.KEYBOARD_KEYS;
    }
    
    private void switchLocMapping(int locPageNum)
    {
        if (locPageNum <= TrainControlUI.NUM_LOC_MAPPINGS && locPageNum >= 1)
        {
            this.locMappingNumber = locPageNum;
        }
         
        if (TrainControlUI.NUM_LOC_MAPPINGS > 1)
        {
            if (this.locMappingNumber == 1)
            {
                this.PrevLocMapping.setEnabled(false);
            }
            else
            {
                this.PrevLocMapping.setEnabled(true);
            }

            if (this.locMappingNumber == TrainControlUI.NUM_LOC_MAPPINGS)
            {
                this.NextLocMapping.setEnabled(false);
            }
            else
            {
                this.NextLocMapping.setEnabled(true);
            }
            
            this.LocMappingNumberLabel.setText(this.getPageName(this.locMappingNumber, false, true));
            
            // Display current mapping number in the tab label
            this.KeyboardTab.setToolTipTextAt(0, "Locomotive Control (Page " + this.locMappingNumber + ")");
           
            // Add page number to icon
            BufferedImage textImage = ImageUtil.generateImageWithText(Integer.toString(this.locMappingNumber), Color.WHITE, new Font("Segoe UI", Font.BOLD, 16), 
                30, 30, 0, 0);

            BufferedImage textImage2 = ImageUtil.generateImageWithText(Integer.toString(this.locMappingNumber), Color.BLACK, new Font("Segoe UI", Font.BOLD, 16), 
                30, 30, 1, 1);

            ImageIcon ic = new javax.swing.ImageIcon(
                ImageUtil.mergeImages(ImageUtil.mergeImages(ImageUtil.convertIconToBufferedImage((ImageIcon) TAB_ICON_CONTROL), textImage2), textImage)
            );

            this.KeyboardTab.setIconAt(0, ic);
       }
       else 
       {
            this.NextLocMapping.setVisible(false);
            this.PrevLocMapping.setVisible(false);
            this.LocMappingNumberLabel.setText("");
       } 
        
       repaintMappings();        
    }
    
    /**
     * Sets the name of the currently active mapping page
     * @param name 
     */
    private void setPageName(String name)
    {
        if (name == null || "".equals(name.trim()))
        {
            this.pageNames.remove(this.locMappingNumber);
        }
        else
        {            
            this.pageNames.put(this.locMappingNumber, name.trim());
        }
        
        this.LocMappingNumberLabel.setText(this.getPageName(this.locMappingNumber, false, true));
        this.repaintLoc(true, null);
    }
    
    /**
     * Optional 3rd argument...
     * @param mappingNumber
     * @param raw
     * @return 
     */
    private String getPageName(int mappingNumber, boolean raw)
    {
        return getPageName(mappingNumber, raw, false);
    }
    
    /**
     * Gets the name of the specified mapping page
     * @param mappingNumber
     * @param raw - do we exclude the page number
     * @return 
     */
    private String getPageName(int mappingNumber, boolean raw, boolean html)
    {
        if (this.pageNames.containsKey(mappingNumber))
        {
            String name = this.pageNames.get(mappingNumber);

            if (!raw)
            {                
                if (html)
                {
                    if (name.length() > TrainControlUI.MAX_PAGE_NAME_LENGTH)
                    {
                        name = name.substring(0, MAX_PAGE_NAME_LENGTH);
                    }
                    
                    name = "<html>" + name + "<br />Page " + mappingNumber + "</html>";
                }
                else
                {
                    if (name.length() > TrainControlUI.MAX_PAGE_NAME_LENGTH_TOP)
                    {
                        name = name.substring(0, MAX_PAGE_NAME_LENGTH_TOP) + "...";
                    }
                    
                    name = "Page " + mappingNumber + " (" + name + ")";
                }
            }
            
            return name;   
        }
        else
        {
            if (raw)
            {
                return "";
            }
            else
            {
                return "Page " + mappingNumber;
            }
        }
    }
    
    /**
     * Prompts the user for the name of the current page
     */
    public void renameCurrentPage()
    {
        String input = JOptionPane.showInputDialog(this, "Enter page " + this.locMappingNumber + " name: ", this.getPageName(this.locMappingNumber, true));
        
        if (input != null)
        {
            this.setPageName(input);
        }
    }
    
    private void switchKeyboard(int keyboardNum)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            if (keyboardNum <= TrainControlUI.NUM_KEYBOARDS && keyboardNum >= 1)
            {
                this.keyboardNumber = keyboardNum;
            }

            if (this.keyboardNumber == 1)
            {
                this.PrevKeyboard.setEnabled(false);
            }
            else
            {
                this.PrevKeyboard.setEnabled(true);
            }

            if (this.keyboardNumber == TrainControlUI.NUM_KEYBOARDS)
            {
                this.NextKeyboard.setEnabled(false);
            }
            else
            {
                this.NextKeyboard.setEnabled(true);
            }

            this.KeyboardNumberLabel.setText("Keyboard " + this.keyboardNumber);

            Integer offset = this.getKeyboardOffset();

            for (Integer i = 1; i <= TrainControlUI.KEYBOARD_KEYS; i++)
            {
                this.switchMapping.get(i).setText((Integer.valueOf(i + offset)).toString());

                // Set the font size dynamically 
                int fontSize = i + offset > 960 ? 11 : 14;
                this.switchMapping.get(i).setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, fontSize));
            }

            repaintSwitches();
        }));
    }
    
    /**
     * Fetches and scales a resource icon
     * @param size
     * @param file
     * @return 
     */
    private static ImageIcon getTabIcon(int size, String file)
    {
        ImageIcon icon = new ImageIcon(TrainControlUI.class.getResource("resources/" + file)); 
        Image scaledImage = icon.getImage().getScaledInstance(-1, size, Image.SCALE_SMOOTH); 
        return new ImageIcon(scaledImage);
    }
    
    public void setViewListener(ViewListener listener, CountDownLatch latch) throws IOException
    {
        // Set the model reference
        this.model = listener;
                 
        List<Map<Integer, String>> saveStates = this.restoreState();
        boolean locWasLoaded = false;
        
        for (int j = 0; j < saveStates.size() && j < TrainControlUI.NUM_LOC_MAPPINGS; j++)
        {
            Map<Integer, String> saveState = saveStates.get(j);
     
            for(Integer i : saveState.keySet())
            {
                JButton b = this.buttonMapping.get(i);

                Locomotive l = this.model.getLocByName(saveState.get(i));

                if (l != null && b != null)
                {
                    locWasLoaded = true;
                    
                    // this.model.log("Loading mapping for page " + Integer.toString(j + 1) + ", " + l.getName());
                    this.locMapping.get(j).put(b, l);
                }
            }
        }
        
        boolean savedLocKey = false;
        // Restore page names, active button, and page, which are stored at the end
        if (saveStates.size() > TrainControlUI.NUM_LOC_MAPPINGS)
        {
            this.pageNames = saveStates.get(saveStates.size() - 1);
            
            try
            {
                if (this.pageNames.containsKey(SAVE_KEY_ACTIVE_MAPPING_NUMBER))
                {
                    this.switchLocMapping(Integer.parseInt(this.pageNames.get(SAVE_KEY_ACTIVE_MAPPING_NUMBER)));
                } 

                if (this.pageNames.containsKey(TrainControlUI.SAVE_KEY_ACTIVE_BUTTON) && 
                        this.buttonMapping.containsKey(Integer.valueOf(this.pageNames.get(SAVE_KEY_ACTIVE_BUTTON))))
                {
                    this.displayCurrentButtonLoc(this.buttonMapping.get(Integer.valueOf(this.pageNames.get(SAVE_KEY_ACTIVE_BUTTON))));
                    savedLocKey = true;
                } 
            }
            catch (Exception e)
            {
                this.model.log("Failed to parse stored mapping number or active button.");
            }
        }
             
        // Hide initially
        locCommandPanels.remove(this.locCommandTab);
        locCommandPanels.remove(this.timetablePanel);
        locCommandPanels.remove(this.autoSettingsPanel);
        
        // Add the first locomotive to the mapping if nothing was loaded
        if (!this.model.getLocList().isEmpty() && !locWasLoaded)
        {
            this.currentLocMapping().put(
                QButton, 
                this.model.getLocByName(this.model.getLocList().get(0))
            );
        }
        
        // Display the locomotive
        if (!savedLocKey)
        {
            displayCurrentButtonLoc(QButton);
        }
        
        // Display loc mapping page
        this.switchLocMapping(this.locMappingNumber);
        
        // Generate list of locomotives
        selector = new LocomotiveSelector(this.model, this);
        selector.init();
        
        adder = new AddLocomotive(this.model, this);
        adder.setLocationRelativeTo(this);
        selector.setLocationRelativeTo(this);
        
        // Load autonomy data
        try
        {
            // Read object using ObjectInputStream
            ObjectInputStream obj_in = new ObjectInputStream(
                new FileInputStream(TrainControlUI.AUTONOMY_FILE_NAME)
            );
            
            // Read an object
            Object obj = obj_in.readObject();

            if (obj instanceof String)
            {
                // Cast object
                this.autonomyJSON.setText((String) obj);
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            this.model.log("Failed to read autonomy state from " + TrainControlUI.AUTONOMY_FILE_NAME);   
        }
                        
        // Add list of routes to tab
        refreshRouteList();
                
        // Add list of layouts to tab
        this.LayoutList.setModel(new DefaultComboBoxModel(listener.getLayoutList().toArray()));
                
        // Display keyboard
        this.switchKeyboard(this.keyboardNumber);
        
         // Hide CS3 app button on non-CS3 controllers
        if (!this.model.isCS3())
        {
            this.openCS3AppMenuItem.setEnabled(false);
        }
                
        HandScrollListener scrollListener = new HandScrollListener(InnerLayoutPanel);
        LayoutArea.getViewport().addMouseMotionListener(scrollListener);
        LayoutArea.getViewport().addMouseListener(scrollListener);
             
        this.latencyLabel.setText("Not Connected to Central Station");

        // Start with true to ensure keyboard events register properly
        // If we don't do this, initializing the UI from the EDT will cause issues
        setAlwaysOnTop(true);
        
        // Render layout now that the UI is visible
        repaintLayout();
        
        if (this.model.getLayoutList().isEmpty())
        {
            createAndApplyEmptyLayout(TrainControlUI.DEMO_LAYOUT_OUTPUT_PATH, true);
        }
        
        // Populate statistics tab
        this.stats = new LocomotiveStats(this);
        this.KeyboardTab.add(this.stats, "Stats", this.KeyboardTab.getComponentCount() - 1);
        
        // Set pane icons   
        // this.KeyboardTab.setIconAt(0, TAB_ICON_CONTROL);
        // this.KeyboardTab.setToolTipTextAt(0, "Locomotive Control");
        this.KeyboardTab.setTitleAt(0, "");
        this.KeyboardTab.setIconAt(1, TAB_ICON_LAYOUT);
        this.KeyboardTab.setToolTipTextAt(1, "Layout");
        this.KeyboardTab.setTitleAt(1, "");
        this.KeyboardTab.setIconAt(4, TAB_ICON_ROUTES);
        this.KeyboardTab.setToolTipTextAt(4, "Routes");
        this.KeyboardTab.setTitleAt(4, "");
        this.KeyboardTab.setIconAt(3, TAB_ICON_KEYBOARD);
        this.KeyboardTab.setToolTipTextAt(3, "Signals & Switches");
        this.KeyboardTab.setTitleAt(3, "");
        this.KeyboardTab.setIconAt(2, TAB_ICON_AUTONOMY);
        this.KeyboardTab.setToolTipTextAt(2, "Full Automation");
        this.KeyboardTab.setTitleAt(2, "");
        this.KeyboardTab.setIconAt(5, TAB_ICON_STATS);
        this.KeyboardTab.setToolTipTextAt(5, "Statistics");
        this.KeyboardTab.setTitleAt(5, "");
        this.KeyboardTab.setIconAt(6, TAB_ICON_LOG);
        this.KeyboardTab.setToolTipTextAt(6, "Log");
        this.KeyboardTab.setTitleAt(6, "");
        
        // Remove the default action for the up/down arrow keys
        InputMap inputMap = this.KeyboardTab.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        
        // Monitor for network activity and show a warning if CS2/3 seems unresponsive
        new Thread(() ->
        {            
            try
            {
                Thread.sleep(CAN_MONITOR_DELAY * 1000);
            } catch (InterruptedException ex) { }
            
            if (this.model.getNumMessagesProcessed() == 0
                && this.model.getNetworkCommState()
            )
            {
                String message = "Warning: No CAN messages have been detected from the Central Station for " + CAN_MONITOR_DELAY + " seconds.";
                this.model.log(message);
                
                javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
                {
                    JOptionPane.showMessageDialog(this, message + "\n\nPlease check that broadcasting is enabled in your CS2/3 network settings.");
                }));
            }
        }).start();
        
        // Check for updates
        this.downloadUpdateMenuItem.setVisible(false);
        
        if (this.model != null && this.checkForUpdates.isSelected())
        {
            new Thread(() ->
            { 
                try
                {
                    JSONObject updateInfo = Util.getLatestReleaseInfo(GITHUB_REPO);
                    LATEST_VERSION = Util.parseReleaseVersion(updateInfo);
                    LATEST_DOWNLOAD_URL = Util.parseDownloadURL(updateInfo);
                    
                    if (Util.parseReleaseURL(updateInfo) != null)
                    {
                        UPDATE_URL = Util.parseReleaseURL(updateInfo);
                    }
                    
                    this.model.log("Latest available version is: " + LATEST_VERSION);

                    if (Conversion.compareVersions(LATEST_VERSION, MarklinControlStation.RAW_VERSION) > 0)
                    {
                        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
                        {
                            this.downloadUpdateMenuItem.setVisible(true);
                            this.viewReleasesMenuItem.setText("v" + LATEST_VERSION + " Update Info");
                            this.helpMenu.setText(this.helpMenu.getText() + "*");
                        }));
                    }
                }
                catch (Exception e)
                {
                    this.model.log("Failed to fetch latest update information.");
                    
                    if (this.model.isDebug())
                    {
                        this.model.log(e);
                    }
                }
            }).start();   
        }
                
        // Monitor latency
        if (this.model != null && model.getNetworkCommState() && PING_INTERVAL > 0)
        {            
            model.sendPing(true);
            
            (new Timer()).scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if (model.getTimeSinceLastPing() > 0 && model.getTimeSinceLastPing() > PING_INTERVAL)
                        {
                            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
                            {
                                latencyLabel.setText("Lost network connection");
                                latencyLabel.setForeground(Color.red);
                            }));
                            
                            checkAutoLayoutLatency(PING_INTERVAL);
                        }
                        
                        model.sendPing(false);
                    }
                    catch (Exception e)
                    {
                        model.log("Error sending ping: " + e.getMessage());

                        model.log(e);
                    }
                }
            }, 0, PING_INTERVAL);
        }
        
        // Load autonomy if requested
        if (this.AutoLoadAutonomyMenuItem.isSelected())
        {
            javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
            {
                this.validateButtonActionPerformed(new CustomActionEvent(this, ActionEvent.ACTION_PERFORMED, "", ""));
            }));
        }
                
        // Release the latch
        if (latch != null) latch.countDown();
        
        // Show window - this is now called externally once this call returns        
    }
    
    /**
     * Renders the UI once everything is initialized - to be called externally
     */
    public void display()
    {     
        // Remember window location.  Because isResizable is already false, this will not restore the size
        this.loadWindowBounds();
        
        // Ensure the preference is properly set across the layouts
        this.ensureShowLayoutPreference(); 
        
        // Ensure the locomotive pane is updated
        this.repaintLoc(true, null);
        
        setVisible(true);
             
        // Restore correct preference
        setAlwaysOnTop(prefs.getBoolean(ONTOP_SETTING_PREF, ONTOP_SETTING_DEFAULT)); 
        pack();
        displayMenuBar();
        
        // Dynamically set this to prevent bug where the UI shrinks after computer sleeps
        setMinimumSize(new java.awt.Dimension(this.getSize().width, this.getSize().height));
        setPreferredSize(new java.awt.Dimension(this.getSize().width, this.getSize().height));
        
        if (this.powerOnStartup.isSelected())
        {
            go();
        }
        else if (this.powerOffStartup.isSelected())
        {
            stop();
        }
        
        // TrackDiagramEditor Layout editing only supported on windows
        if (!this.isWindows())// || !model.isDebug())
        {
            this.openLegacyTrackDiagramEditor.setVisible(false);
        }
        
        // Debug mode indicator
        if (this.model.isDebug())
        {
            setTitle(this.getTitle() + " [Debug]");
        }
                
        restoreLayoutTitles();
    }
    
    /**
     * Shows or hides the menu bar depending on the user's preference
     */
    private void displayMenuBar()
    {
        int barHeight = this.mainMenuBar.getHeight(); // 23
        
        int minHeight = this.getMinimumSize().height;
        int maxHeight = this.getMaximumSize().height;
        int prefHeight = this.getPreferredSize().height;
        int sizeHeight = this.getSize().height;

        if (this.mainMenuBar.isVisible() && !prefs.getBoolean(MENUBAR_SETTING_PREF, true))
        {
            this.mainMenuBar.setVisible(false);
            
            setMinimumSize(new java.awt.Dimension(this.getMinimumSize().width, minHeight - barHeight));
            setPreferredSize(new java.awt.Dimension(this.getPreferredSize().width, prefHeight - barHeight));            
            setMaximumSize(new java.awt.Dimension(this.getMaximumSize().width, maxHeight - barHeight));
            setSize(new java.awt.Dimension(this.getSize().width, sizeHeight - barHeight));

            revalidate();
            repaint();
        }
        else if (!this.mainMenuBar.isVisible() && prefs.getBoolean(MENUBAR_SETTING_PREF, true))
        {            
            setMaximumSize(new java.awt.Dimension(this.getMaximumSize().width, maxHeight + barHeight));
            setPreferredSize(new java.awt.Dimension(this.getPreferredSize().width, prefHeight + barHeight));
            setMinimumSize(new java.awt.Dimension(this.getMinimumSize().width, minHeight + barHeight));
            setSize(new java.awt.Dimension(this.getSize().width, sizeHeight + barHeight));
            
            this.mainMenuBar.setVisible(true);

            revalidate();
            repaint();
        }
    }
    
    /**
     * Updates the latency value
     * @param latency
     */
    @Override
    public void updateLatency(double latency)
    { 
        if (PING_INTERVAL > 0)
        {
            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
            {
                this.latencyLabel.setText("CS" + (this.model.isCS3() ? "3" : "2") + " Network Latency: " + String.format("%.0f", latency) + "ms");

                this.latencyLabel.setForeground(latency > PING_ORANGE ? (latency > PING_RED ? Color.RED : Color.MAGENTA) : Color.BLACK);
            }));
            
            this.checkAutoLayoutLatency(latency);
        }        
    }
    
    /**
     * Turns off the power in autonomy mode when the set latency limit is exceeded
     * @param latency 
     */
    private void checkAutoLayoutLatency(double latency)
    {
        if (model.getPowerState())
        {
            Layout l = model.getAutoLayout();

            if (l != null && l.isRunning() && l.getMaxLatency() > 0 && latency > l.getMaxLatency())
            {
                model.log("Turning off power because network latency exceeded " + l.getMaxLatency() + "ms while in autonomy mode.");

                model.stop();
                
                javax.swing.SwingUtilities.invokeLater(new Thread(() ->
                {
                    JOptionPane.showMessageDialog(
                        this, 
                        "The power was turned off because the network latency exceeded " + l.getMaxLatency() + "ms while in autonomy mode.\n\nCheck your network connection before restoring power, or update this threshold in the autonomy settings."
                    );
                }));
            }
        }
    }
    
    /**
     * Self-contained method to initialize a blank layout
     * @param folderName
     * @param verify
     */
    public void createAndApplyEmptyLayout(String folderName, boolean verify)
    {
        boolean proceed;
        
        if (!verify)
        {
            proceed = true;
        }
        else
        {        
            // Attempt to create an empty layout if needed
            int dialogResult = JOptionPane.showConfirmDialog(
                this, "Do you want to initialize a new track diagram?\n\nLayout files will be written to: \n" + 
                        new File(folderName).getAbsolutePath(),
                "Create New Track Diagram", JOptionPane.YES_NO_OPTION
            );
            
            proceed = (dialogResult == JOptionPane.YES_OPTION);
        }
        
        if (proceed)
        {
            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
            {
                try
                {
                    this.model.log("No layout detected. Initializing local demo layout...");
                    String path = this.initializeEmptyLayout(folderName);

                    if (path != null)
                    {
                        this.model.log("Layout initialized at: " + path);
                        prefs.put(LAYOUT_OVERRIDE_PATH_PREF, path);

                        this.model.syncWithCS2();
                        this.repaintLoc();
                    }

                    if (!this.model.getLayoutList().isEmpty())
                    {
                        this.initializeTrackDiagram(false);
                    }
                    else
                    {
                        this.model.log("Failed to initialize demo layout.");
                        JOptionPane.showMessageDialog(this, "Failed to initialize demo layout.");
                    }
                }
                catch (Exception e)
                {
                    this.model.log("Critical error while initializing demo layout: " + e.getMessage());

                    this.model.log(e);
                }
                
                this.initializeLocalLayoutMenuItem.setEnabled(true);      
            }));
        }
        else
        {
            this.initializeLocalLayoutMenuItem.setEnabled(true);
        }
    }
    
    /**
     * Shows the layout tab and re-loads the track diagram
     * @param showTab
     */
    synchronized public void initializeTrackDiagram(boolean showTab)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(()->
        { 
            this.LayoutList.setModel(new DefaultComboBoxModel(this.model.getLayoutList().toArray()));

            if (!this.model.getLayoutList().isEmpty())
            {
                this.repaintLoc();
                this.repaintLayout(showTab, false);
            }
            else
            {
                this.model.log("Model error: no layout loaded.");
            }
        }));
    }
    
    public LocomotiveSelector getLocSelector()
    {
        return this.selector;
    }
    
    private void switchF(int fn)
    {        
        if (this.activeLoc != null)
        {
            this.fireF(fn, !this.activeLoc.getF(fn));
        }
    }
    
    private void fireF(int fn, boolean state)
    {        
        if (this.activeLoc != null)
        {            
            if (this.activeLoc.isFunctionTimed(fn) > 0)
            {
                new Thread(() -> {
                    this.activeLoc.toggleF(fn, this.activeLoc.isFunctionTimed(fn) * 1000);
                }).start();
            }
            else if (this.activeLoc.isFunctionPulse(fn))
            {
                new Thread(() -> {
                    this.activeLoc.toggleF(fn, MarklinLocomotive.PULSE_FUNCTION_DURATION);
                }).start();
            }
            else
            {
                new Thread(() -> {
                    this.activeLoc.setF(fn, state);
                }).start();
            }
        }
    }
    
    public void selectLocomotiveActivated(JButton button)
    {
        // Make sure this button is selected
        if (button != null)
        {
            button.doClick();
            showLocSelector();
        }
    }
    
    public void showLocSelector()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(()->
        { 
            if (this.getModel().getLocomotives().isEmpty())
            {
                JOptionPane.showMessageDialog(this,
                    NO_LOC_MESSAGE
                ); 
            }
            else
            {
                this.selector.setVisible(true);
                this.selector.updateScrollArea();
                this.selector.requestFocus();
                this.selector.toFront();
            }
        }));
    }
    
    /**
     * Clipboard copy
     * @param button 
     * @param cut 
     */
    public void setCopyTarget(JButton button, boolean cut)
    {
        copyTarget = this.currentLocMapping().get(button);
        copyTargetButton = button;
        copyTargetPage = this.locMappingNumber;
        
        if (cut)
        {
            Locomotive copyTargetTemp = copyTarget;
            copyTarget = null;
            doPaste(copyTargetButton, false, false);
            copyTarget = copyTargetTemp;
        }
        
        // Put locomotive name in clipboard
        if (button != null && copyTarget != null)
        {
            StringSelection selection = new StringSelection(copyTarget.getName());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        }
    }
    
    /**
     * Copies a locomotive mapping to the next page
     * @param button 
     */
    public void copyToNextPage(JButton button)
    {        
        this.nextLocMapping().put(button, this.currentLocMapping().get(button));
        
        if (button.equals(this.currentButton))
        {
            displayCurrentButtonLoc(this.currentButton);
        }
    }
    
    /**
     * Copies a locomotive mapping to the previous page
     * @param button 
     */
    public void copyToPrevPage(JButton button)
    {        
        this.prevLocMapping().put(button, this.currentLocMapping().get(button));
        
        if (button.equals(this.currentButton))
        {
            displayCurrentButtonLoc(this.currentButton);
        }
    }
    
    /**
     * Has the copy target been set?
     * @return 
     */
    public boolean hasCopyTarget()
    {
        return copyTarget != null;
    }
    
    public void clearCopyTarget()
    {
        copyTarget = null;
        copyTargetButton = null;
        copyTargetPage = 0;
    }
    
    public boolean buttonHasLocomotive(JButton b)
    {
        return this.currentLocMapping().get(b) != null;
    }
    
    public Locomotive getButtonLocomotive(JButton b)
    {
        return this.currentLocMapping().get(b);
    }
    
    /**
     * Gets the locomotive which will be swapped to where the pasted loc came from
     * @return 
     */
    public Locomotive getSwapTarget()
    {
        if (this.copyTargetPage > 0)
        {
            return this.locMapping.get(this.copyTargetPage - 1).get(this.copyTargetButton);
        }
        
        return null;
    }
    
    /**
     * Gets the locomotive to be pasted
     * @return 
     */
    public Locomotive getCopyTarget()
    {
        return copyTarget;
    }
    
    /**
     * Gets the active locomotive
     * @return 
     */
    public Locomotive getActiveLoc()
    {
        return activeLoc;
    }
    
    /**
     * Apply function presets
     * @param l 
     */
    public void applyPreferredFunctions(Locomotive l)
    {
        if (l != null)
        {
            new Thread(() ->
            {
                this.model.getLocByName(l.getName()).applyPreferredFunctions();
                
                // Ensure correct cascading
                for (MarklinLocomotive other : this.model.getLocByName(l.getName()).getLinkedLocomotives().keySet())
                {
                    other.applyPreferredFunctions();
                }
                
            }).start();
        }
    }
    
    public void applyPreferredSpeed(Locomotive l)
    {
        if (l != null)
        {
            new Thread(() -> {
                this.model.getLocByName(l.getName()).applyPreferredSpeed();
            }).start();
        }
    }
    
    /**
     * Save function presets
     * @param l 
     */
    public void savePreferredFunctions(Locomotive l)
    {
        if (l != null)
        {
            new Thread(() ->
            {
                this.model.getLocByName(l.getName()).savePreferredFunctions();
            }).start();
        }
    }
    
    public void savePreferredSpeed(Locomotive l)
    {
        if (l != null)
        {
            new Thread(() ->
            {
                this.model.getLocByName(l.getName()).savePreferredSpeed();
            }).start();
        }
    }
    
    public void locFunctionsOff(Locomotive l)
    {
        if (l != null)
        {
            new Thread(() ->
            {
                this.model.locFunctionsOff(this.model.getLocByName(l.getName()));
                
                // Ensure correct cascading
                for (MarklinLocomotive other : this.model.getLocByName(l.getName()).getLinkedLocomotives().keySet())
                {
                    this.model.locFunctionsOff(other);
                }
                
            }).start();
        }
    }
    
    /**
     * Synchronizes the state of a locomotive w/ the Central Station
     * @param l 
     */
    public void syncLocomotive(Locomotive l)
    {
        if (l != null)
        {
            javax.swing.SwingUtilities.invokeLater(new Thread(() -> {
                this.model.syncWithCS2();
                this.model.syncLocomotive(l.getName());
                repaintLoc(true, null);
                this.repaintLayout();
                this.repaintMappings(Collections.singletonList(l), true);
            }));
        }   
    }
        
    /**
     * Pastes to copied locomotive to a given UI button
     * @param b 
     * @param swap - swaps the copied locomotive with the source
     * @param move - (if swap is false) this will clear the source button
     */
    public void doPaste(JButton b, boolean swap, boolean move)
    {
        if (b != null)
        {
            // Move the current locomotive to the source of the copy
            if (swap && this.copyTargetPage > 0)
            {
                this.locMapping.get(this.copyTargetPage - 1).put(copyTargetButton, this.currentLocMapping().get(b));

                // If we are swapping to the same button that is currently active, activate the paste target so that everything gets repainted correctly
                if (copyTargetButton.equals(this.currentButton))
                {
                    displayCurrentButtonLoc(b);
                }
            }
            // Clear the source of the copy
            else if (move && this.copyTargetPage > 0)
            {
                this.locMapping.get(this.copyTargetPage - 1).put(copyTargetButton, null);
                
                if (copyTargetButton.equals(this.currentButton))
                {
                    displayCurrentButtonLoc(b);
                }
            }
            
            this.currentLocMapping().put(b, copyTarget);

            // If we are pasting to the same button that is currently active, activate the paste target (on the current page)
            if (b.equals(this.currentButton))
            {
                displayCurrentButtonLoc(this.currentButton);
            }
            
            repaintMappings();  
            
            clearCopyTarget();
        }
    }
    
    public void mapLocToCurrentButton(String s)
    {
        mapLocToCurrentButton(s, false);
    }
    
    public void mapLocToCurrentButton(String s, boolean emptyOnly)
    {
        Locomotive l = this.model.getLocByName(s);
         
        if (l != null)
        {
            // Skip if something is already mapped
            if (emptyOnly && this.currentLocMapping().get(this.currentButton) != null) return;
            
            // Unset if same as current loc
            if (this.currentLocMapping().get(this.currentButton) != null &&
                    this.currentLocMapping().get(this.currentButton).equals(l))
            {
                this.currentLocMapping().put(this.currentButton, null);
            }
            else
            {
                this.currentLocMapping().put(this.currentButton, l);
            }
            displayCurrentButtonLoc(this.currentButton);
        }
        
        this.repaintMappings();
    }
    
    private synchronized void repaintMappings()
    {
        repaintMappings(null, false);
    }
    
    /**
     * Repaints the locomotive keyboard
     * @param forceUpdateLoc 
     */
    private synchronized void repaintMappings(List<Locomotive> forceUpdateLoc, boolean updateIcon)
    {         
        this.MappingRenderer.submit(new Thread(() -> 
        { 
            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
            {                
                Set<JButton> update;
                
                if (forceUpdateLoc == null)
                {   
                    update = this.labelMapping.keySet();
                }
                else
                {
                    update = this.currentLocMapping(forceUpdateLoc).keySet();
                }
                
                // Only repaint a button if the locomotive has changed
                for (JButton b : update)
                {
                    // Grey out if the active page corresponds to the active loc
                    if (b.equals(this.currentButton) 
                            && this.lastLocMappingPainted == this.locMappingNumber)
                    {
                        b.setEnabled(false);
                        this.labelMapping.get(b).setForeground(Color.red);
                    }
                    else
                    {
                        b.setEnabled(true);
                        this.labelMapping.get(b).setForeground(Color.black);
                    }

                    Locomotive l = this.currentLocMapping().get(b);  

                    if (l != null 
                            && this.model.getLocByName(l.getName()) != null // If this loc no longer exists, don't display it
                    )
                    {                        
                        String name = l.getName();

                        if (name.length() > 9)
                        {
                            name = name.substring(0, 9);
                        }

                        if (!this.labelMapping.get(b).getText().equals(name) || updateIcon) 
                        {
                            this.labelMapping.get(b).setText(name); 
                            this.labelMapping.get(b).setCaretPosition(0);
                            repaintIcon(b, l, this.locMappingNumber);
                        }

                        this.sliderMapping.get(b).setEnabled(true);
                        this.sliderMapping.get(b).setValue(l.getSpeed());      
                    }
                    else
                    {
                        if (!this.labelMapping.get(b).getText().equals("---"))
                        {
                            this.labelMapping.get(b).setText("---");
                            repaintIcon(b, l, this.locMappingNumber);
                        }

                        this.sliderMapping.get(b).setValue(0);
                        this.sliderMapping.get(b).setEnabled(false);   
                    }
                }
            }));
        }));
    }
    
    @Override
    public void updatePowerState()
    {
        if (this.model.getPowerState())
        {
            this.PowerOff.setEnabled(true);
            this.OnButton.setEnabled(false);
        }
        else
        {
            this.PowerOff.setEnabled(false);
            this.OnButton.setEnabled(true);
        }
    }
        
    /**
     * Repaints a single switch on the keyboard
     * @param address 
     * @param type 
     */
    @Override
    synchronized public void repaintSwitch(int address, Accessory.accessoryDecoderType type)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            int offset = this.getKeyboardOffset();
            
            if (offset + 1 <= address && offset + TrainControlUI.KEYBOARD_KEYS >= address)
            {
                JToggleButton key = this.switchMapping.get(address - offset);

                if (this.model.getAccessoryState(address, getKeyboardProtocol()))
                {
                    key.setSelected(true);
                    key.setBackground(COLOR_SWITCH_RED);        
                }
                else
                {
                    key.setSelected(false);
                    key.setBackground(COLOR_SWITCH_GREEN);
                }

                // Underline when red
                Font font = key.getFont();
                Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
                attributes.put(TextAttribute.UNDERLINE, key.isSelected() ? TextAttribute.UNDERLINE_ON : -1);
                key.setFont(font.deriveFont(attributes));
            }
            
            if (this.routeEditor != null || (this.graphViewer != null && this.graphViewer.getGraphEdgeEditor() != null))
            {
                // Throttle to ensure commands are not duplicated
                long currentTime = System.currentTimeMillis(); 
                String command = this.model.getAccessoryByAddress(address, type).toAccessorySettingString();

                if (!command.isEmpty() && 
                        (!command.equals(lastCapturedAccessoryCommand) || (currentTime - lastCapturedAccessoryCommandTime) > CAPTURE_COMMAND_THROTTLE))
                { 
                    lastCapturedAccessoryCommand = command; 
                    lastCapturedAccessoryCommandTime = currentTime; 

                    // Pass the event to the route editor if we are capturing commands
                    if (this.routeEditor != null && routeEditor.isVisible() && this.routeEditor.isCaptureCommandsSelected())
                    {
                        this.routeEditor.appendCommand(command);
                    }

                    // Pass the event to the lock edge editor
                    if (this.graphViewer != null && this.graphViewer.getGraphEdgeEditor() != null && this.graphViewer.getGraphEdgeEditor().isCaptureCommandsSelected())
                    {
                        this.graphViewer.getGraphEdgeEditor().appendCommand(command);
                    }
                }
            }
        }));
    }
    
    public void mapUnassignedLocomotives()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            for (JButton key : this.labelMapping.keySet())
            {
                // Is the key free?
                if (this.currentLocMapping().get(key) == null || this.model.getLocByName(this.currentLocMapping().get(key).getName()) == null)
                {
                    List<String> locs = this.model.getLocList();
                    Collections.sort(locs);
                    
                    for (String s : locs)
                    {
                        Locomotive l = this.model.getLocByName(s);
                        
                        // Is the locomotive mapped anywhere?
                        if (l != null && this.getAllLocButtonMappings(l).isEmpty())
                        {
                            this.currentLocMapping().put(key, l);
                            break;
                        }
                    }
                }
            }

            repaintMappings();
        }));
    }
    
    @Override
    public void repaintSwitches()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            int offset = this.getKeyboardOffset();
            
            // DCC-only addresses
            if (this.keyboardNumber >= 6)
            {
                this.DCC.setSelected(true);                  
            }
            else
            {
                this.MM2.setSelected(prefs.getBoolean(PREFERRED_KEYBOARD_MM2, true));
            }  

            for (int i = 1; i <= TrainControlUI.KEYBOARD_KEYS; i++)
            {
                JToggleButton key = this.switchMapping.get(i);
                
                if (key != null)
                {           
                    if (this.model.getAccessoryState(i + offset, getKeyboardProtocol()))
                    {
                        key.setSelected(true);
                        key.setBackground(COLOR_SWITCH_RED);        
                    }
                    else
                    {
                        key.setSelected(false);
                        key.setBackground(COLOR_SWITCH_GREEN);
                    }

                    // Underline when red
                    Font font = key.getFont();
                    Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
                    attributes.put(TextAttribute.UNDERLINE, key.isSelected() ? TextAttribute.UNDERLINE_ON : -1);
                    key.setFont(font.deriveFont(attributes));
                }
            }
        }));
    }
    
    /**
     * Visually configures a button without an image
     * @param b 
     */
    private void noImageButton(JButton b)
    {
        b.setContentAreaFilled(true);
        b.setIcon(null);
        b.setForeground(new java.awt.Color(0, 0, 0));
        b.setOpaque(false);
    }
    
    /**
     * Returns a scaled locomotive image
     * @param url
     * @param size
     * @return
     * @throws IOException 
     */
    public Image getLocImage(String url, int size) throws IOException, Exception
    {
        String key = url + Integer.toString(size);
        
        if (!TrainControlUI.getImageCache().containsKey(key))
        {
            Image img = ImageIO.read(new URL(url));
            
            if (img != null)
            {
                float aspect = (float) img.getHeight(null) / (float) img.getWidth(null);
                TrainControlUI.getImageCache().put(key, 
                    // img.getScaledInstance(size, (int) (size * aspect), 1)
                    // Higher quality scaling
                    ImageUtil.getScaledImage(ImageUtil.toTransparentBufferedImage(img), size, (int) (size * aspect))
                );
            }
        }

        return TrainControlUI.getImageCache().get(key);        
    }
    
    /**
     * Returns a scaled locomotive image
     * @param url
     * @param size
     * @param maxHeight
     * @return
     * @throws IOException 
     */
    public Image getLocImageMaxHeight(String url, int size, int maxHeight) throws IOException
    {
        String key = url + Integer.toString(size);
        
        if (!TrainControlUI.getImageCache().containsKey(key))
        {
            Image img = ImageIO.read(new URL(url));
            
            if (img != null)
            {
                float aspect = (float) img.getHeight(null) / (float) img.getWidth(null);
                
                // Limit maximum height
                if (size * aspect > maxHeight)
                {
                    size = (int) (size * (maxHeight / (size * aspect)));
                }
                
                TrainControlUI.getImageCache().put(key, 
                    // img.getScaledInstance(size, (int) (size * aspect), 1)
                    // Higher quality scaling
                    ImageUtil.getScaledImage(ImageUtil.toTransparentBufferedImage(img), size, (int) (size * aspect))
                );
            }
        }

        return TrainControlUI.getImageCache().get(key);        
    }
    
    /**
     * Repaints a locomotive button
     * @param b
     * @param l 
     * @param correspondingLocMappingNumber
     */
    private void repaintIcon(JButton b, Locomotive l, Integer correspondingLocMappingNumber)
    {
        noImageButton(b); // makes icons appear consistent on slow networks
        
        ImageLoader.submit(new Thread(() -> 
        {
            if (b != null)
            {
                if (l == null)
                {
                    noImageButton(b);
                }
                else if (LOAD_IMAGES && l.getImageURL() != null && l.getImageURL().length() > 0)
                {
                    try 
                    {
                        BufferedImage locImage = (BufferedImage) getLocImage(l.getImageURL(), 66);
                      
                        // Generate shadow images
                        BufferedImage textImage = ImageUtil.generateImageWithText(b.getText(), Color.BLACK, b.getFont(), 
                            locImage.getWidth(null), locImage.getHeight(null), 2, 1);
                        
                        BufferedImage textImage2 = ImageUtil.generateImageWithText(b.getText(), Color.BLACK, b.getFont(), 
                            locImage.getWidth(null), locImage.getHeight(null), 1, -1);
                        
                        ImageIcon ic = new javax.swing.ImageIcon(
                            ImageUtil.mergeImages(ImageUtil.mergeImages(locImage, textImage), textImage2)
                        );
                        
                        // The active page has changed since this thread was called.  No need to update the UI.
                        if (this.locMappingNumber != correspondingLocMappingNumber)
                        {
                            return;
                        }
                        
                        b.setIcon(ic);  

                        b.setHorizontalTextPosition(SwingConstants.CENTER);

                        b.setForeground(Color.WHITE);
                        b.setContentAreaFilled(false);
                    } 
                    catch (Exception e)
                    {
                        noImageButton(b);

                        this.model.log("Failed to load image " + l.getImageURL());
                        
                        this.model.log(e);
                    }
                }
                else
                {
                    noImageButton(b);
                }
            }
        }));
    }
    
    public ExecutorService getImageLoader()
    {
        return this.ImageLoader;
    }
    
    @Override
    public void repaintLoc()
    {
        repaintLoc(false, null);
    }
    
    /**
     * Gets the locomotive name to display in pop-up window titles
     * @return 
     */
    public String getWindowTitleString()
    {
        // Show nothing if there is no active locomotive, or the setting is not selected
        if (this.activeLoc == null || this.model.getLocByName(this.activeLoc.getName()) == null || !this.activeLocInTitle.isSelected())
        {
            return "";
        }
        else
        {
            return " [" + this.activeLoc.getName() + " " + 
                    (this.activeLoc.getDirection() == Locomotive.locDirection.DIR_FORWARD ? ">>" : "<<") +
                    " " + this.activeLoc.getSpeed() + "%]";
        }
    }
        
    @Override
    synchronized public void repaintLoc(boolean force, List<Locomotive> updatedLocs)
    {     
        // Prevent concurrent calls
        for (Future<?> f : this.locFutures)
        {
            if (!f.isDone()) 
            {
                return;
            }
        }

        this.locFutures.clear();

        this.locFutures.add(
            this.LocRenderer.submit(new Thread(() -> 
            { 
                javax.swing.SwingUtilities.invokeLater(new Thread(() ->
                {
                    if (this.activeLoc != null 
                            && this.model.getLocByName(this.activeLoc.getName()) != null // If this loc no longer exists, don't display it
                    )
                    {           
                        // Only update if the active locomotive matches the event
                        if (updatedLocs == null || updatedLocs.contains(activeLoc))
                        {
                            String name = this.activeLoc.getName();

                            if (name.length() > MAX_LOC_NAME)
                            {
                                name = name.substring(0, MAX_LOC_NAME);
                            }
                            
                            // Pre-compute this so we can check if it has changed
                            // "Page " + this.currentButtonlocMappingNumber + " Button "
                            String locLabel = "<html><nobr>" + this.currentButton.getText() + " &#8226; " 
                                    + this.getPageName(currentButtonlocMappingNumber, false, false) + " &#8226; "
                                    + this.activeLoc.getDecoderTypeLabel() 
                                    + " " + this.model.getLocAddress(this.activeLoc.getName())
                                    + "</nobr></html>";
                                                        
                            // Display active locomotive in autonomy UI
                            String windowTitleString = getWindowTitleString();
                            
                            if (this.graphViewer != null)
                            {
                                this.graphViewer.setTitle(GraphViewer.WINDOW_TITLE + windowTitleString);
                            }
                            
                            for (LayoutPopupUI popup : this.popups)
                            {
                                popup.setTitle(popup.getLayoutTitle() + windowTitleString);
                            } 

                            // Only repaint icon if the locomotive is changed
                            // Visual stuff
                            if (!this.ActiveLocLabel.getText().equals(name) || !locLabel.equals(CurrentKeyLabel.getText()) || force)
                            {
                                // Do this outside of the queue b/c otherwise it would be delayed for the currently selected loc at app startup
                                repaintIcon(this.currentButton, this.activeLoc, currentButtonlocMappingNumber);

                                ImageLoaderLoc.submit(new Thread(() -> 
                                {
                                    if (LOAD_IMAGES && this.activeLoc.getImageURL() != null)
                                    {
                                        try 
                                        {
                                            locIcon.setIcon(new javax.swing.ImageIcon(
                                                getLocImageMaxHeight(this.activeLoc.getImageURL(), LOC_ICON_WIDTH, LOC_ICON_HEIGHT)
                                                // getLocImage(this.activeLoc.getImageURL(), LOC_ICON_WIDTH)
                                            ));      
                                            locIcon.setText("");
                                            locIcon.setVisible(true);
                                        }
                                        catch (IOException e)
                                        {
                                            locIcon.setIcon(null);
                                            locIcon.setVisible(false);
                                        }
                                    }
                                    else
                                    {
                                        locIcon.setIcon(null);
                                        locIcon.setVisible(false);
                                    }
                                }));

                                this.ActiveLocLabel.setText(name);

                                this.CurrentKeyLabel.setText(locLabel);

                                for (int i = 0; i < this.activeLoc.getNumF(); i++)
                                {
                                    final JToggleButton bt = this.rFunctionMapping.get(i);
                                    final int functionType = this.activeLoc.getFunctionType(i);

                                    bt.setVisible(true);
                                    bt.setEnabled(true);

                                    // Use "active" icons on the CS3, which look better
                                    String targetURL = this.activeLoc.getFunctionIconUrl(i, functionType, this.model.isCS3() || !this.model.getNetworkCommState(), true);

                                    final boolean hasCustom = this.activeLoc.getLocalFunctionImageURL(i) != null;

                                    bt.setHorizontalTextPosition(JButton.CENTER);
                                    bt.setVerticalTextPosition(JButton.CENTER);

                                    ImageLoaderLoc.submit(new Thread(() -> 
                                    {
                                        try
                                        {
                                            if ((hasCustom || functionType > 0) && LOAD_IMAGES)
                                            {
                                                Image icon = getLocImage(targetURL, BUTTON_ICON_WIDTH);

                                                if (icon != null)
                                                {
                                                    bt.setIcon(
                                                        new javax.swing.ImageIcon(
                                                            icon
                                                        )
                                                    );

                                                    bt.setText("");                                    
                                                }
                                                else
                                                {
                                                    //bt.setText("F" + Integer.toString(fNo));
                                                }
                                            }
                                            else
                                            {
                                                bt.setIcon(null);
                                                bt.setText("");                                    
                                                //bt.setText("F" + Integer.toString(fNo));
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            this.model.log("Icon not found: " + targetURL);
                                            //bt.setText("F" + Integer.toString(fNo));
                                        } 
                                    }));         
                                }

                                for (int i = this.activeLoc.getNumF(); i < NUM_FN; i++)
                                {
                                    this.rFunctionMapping.get(i).setVisible(true);
                                    this.rFunctionMapping.get(i).setEnabled(false);

                                    //this.rFunctionMapping.get(i).setText("F" + Integer.toString(i));
                                    this.rFunctionMapping.get(i).setText("");                                    
                                    this.rFunctionMapping.get(i).setIcon(null);
                                }

                                // Remember the active tab
                                int currentFIndex = FunctionTabs.getSelectedIndex();
                                
                                // Hide unnecessary function tabs
                                if (this.activeLoc.getNumF() < 20)
                                {
                                    FunctionTabs.remove(this.F20AndUpPanel);
                                }
                                else
                                {
                                    FunctionTabs.add("F20-F31", this.F20AndUpPanel);
                                    FunctionTabs.setSelectedIndex(currentFIndex);
                                }

                                this.Backward.setVisible(true);
                                this.Forward.setVisible(true);
                                this.SpeedSlider.setVisible(true);
                                this.FunctionTabs.setVisible(true);
                            }

                            // Loc state
                            if (this.activeLoc.goingForward())
                            {
                                this.Forward.setSelected(true);
                                this.Backward.setSelected(false);
                            }
                            else
                            {
                                this.Backward.setSelected(true);
                                this.Forward.setSelected(false);
                            }

                            for (int i = 0; i < this.activeLoc.getNumF(); i++)
                            {
                                this.rFunctionMapping.get(i).setSelected(this.activeLoc.getF(i));
                            }

                            for (int i = this.activeLoc.getNumF(); i < NUM_FN; i++)
                            {
                                this.rFunctionMapping.get(i).setSelected(this.activeLoc.getF(i));
                            }

                            this.SpeedSlider.setValue(this.activeLoc.getSpeed()); 
                        }
                    }
                    else
                    {
                        locIcon.setIcon(null);
                        locIcon.setText("");

                        this.ActiveLocLabel.setText("No Locomotive (Click here)");

                        this.CurrentKeyLabel.setText("<html><nobr>" + this.currentButton.getText() + " &#8226; " 
                                + this.getPageName(currentButtonlocMappingNumber, false, false) + "</nobr></html>"    
                        );

                        this.Backward.setVisible(false);
                        this.Forward.setVisible(false);
                        this.SpeedSlider.setVisible(false);
                        this.FunctionTabs.setVisible(false);

                        for (int i = 0; i < NUM_FN; i++)
                        {
                            this.rFunctionMapping.get(i).setVisible(false);
                        }
                        
                        // Clear locomotive from graph UI title
                        if (this.graphViewer != null)
                        {
                            this.graphViewer.setTitle(GraphViewer.WINDOW_TITLE);
                        }
                        
                        for (LayoutPopupUI popup : this.popups)
                        {
                            popup.setTitle(popup.getLayoutTitle()); 
                        }
                    }
                    
                    // Repaint mappings only if the updated locomotive is currently visible
                    if (updatedLocs == null || currentLocMapping().values().stream().anyMatch(updatedLocs::contains))
                    {
                        this.repaintMappings(updatedLocs, false);
                    }
                }));
            }))
        );
    }
    
    private Map<JButton, Locomotive> nextLocMapping()
    {
        return this.locMapping.get(this.locMappingNumber % TrainControlUI.NUM_LOC_MAPPINGS);
    }
    
    private Map<JButton, Locomotive> prevLocMapping()
    {
        return this.locMapping.get(Math.floorMod(this.locMappingNumber - 2, TrainControlUI.NUM_LOC_MAPPINGS));
    }
        
    private Map<JButton, Locomotive> currentLocMapping()
    {
        return this.locMapping.get(this.locMappingNumber - 1);
    }
    
    /**
     * Same as currentLocMapping, but filters on the locomotives in the passed array
     * @param l
     * @return 
     */
    private Map<JButton, Locomotive> currentLocMapping(List<Locomotive> l)
    {
        Map<JButton, Locomotive> result = new HashMap<>();
        
        for (Map.Entry<JButton, Locomotive> entry : this.currentLocMapping().entrySet())
        {
            if (l.contains(entry.getValue()))
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
    
    private void displayCurrentButtonLoc(javax.swing.JButton b)
    {
        displayCurrentButtonLoc(b, false);
    }
    
    private void displayCurrentButtonLoc(javax.swing.JButton b, boolean showSelector)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            if (this.currentButton != null)
            {
                this.currentButton.setEnabled(true);
                this.labelMapping.get(this.currentButton).setForeground(Color.black);
            }

            if (b != null)
            {
                this.currentButton = b;
                this.currentButtonlocMappingNumber = this.locMappingNumber;

                this.currentButton.setEnabled(false);

                Locomotive current = this.currentLocMapping().get(this.currentButton);

                if (current != null)
                {
                    this.activeLoc = this.model.getLocByName(current.getName());
                }
                else
                {
                    this.activeLoc = null;
                }

                this.labelMapping.get(this.currentButton).setForeground(Color.red);
            }

            this.lastLocMappingPainted = this.locMappingNumber;

            repaintLoc();  
            repaintMappings();
            
            if (showSelector)
            {
                // Show selector if no locomotive is assigned and clipboard is empty
                if (this.activeLoc == null && this.copyTarget == null)
                {
                    showLocSelector();
                }
            }
        }));
    }

    private void setLocSpeed(int speed)
    {
        if (this.activeLoc != null)
        {
            new Thread(() ->
            {
                this.activeLoc.setSpeed(speed);
            }).start();
            //repaintLoc();  not needed because the network will update it
        }
    }     
    
    private void stopLoc()
    {
        if (this.activeLoc != null)
        {
            new Thread(() ->
            {
                this.activeLoc.instantStop();
            }).start();
        }
    }
    
    private void backwardLoc()
    {
        if (this.activeLoc != null) // && this.activeLoc.goingForward())
        {
            new Thread(() ->
            {
                this.activeLoc.stop().setDirection(Locomotive.locDirection.DIR_BACKWARD);
                this.Forward.setSelected(false);
                this.Backward.setSelected(true);
            }).start();
        } 
    }
    
    private void forwardLoc()
    {
        if(this.activeLoc != null) // && this.activeLoc.goingBackward())
        {
            new Thread(() ->
            {
                this.activeLoc.stop().setDirection(Locomotive.locDirection.DIR_FORWARD);
                this.Forward.setSelected(true);
                this.Backward.setSelected(false);
            }).start();
        }
    }
    
    private void switchDirection()
    {
        if(this.activeLoc != null)
        {
             if (this.activeLoc.goingForward())
             {
                 backwardLoc();
             }
             else
             {
                 forwardLoc();
             }
        }  
    }
      
    private void go()
    {
        new Thread(() ->
        {
            this.model.go();
        }).start();
    }
    
    private void stop()
    {
        new Thread(() ->
        {
            this.model.stop();
        }).start();
    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        KeyboardTab = new javax.swing.JTabbedPane();
        LocControlPanel = new javax.swing.JPanel();
        locMappingLabel = new javax.swing.JLabel();
        LocContainer = new javax.swing.JPanel();
        CButton = new javax.swing.JButton();
        VButton = new javax.swing.JButton();
        ZButton = new javax.swing.JButton();
        XButton = new javax.swing.JButton();
        MButton = new javax.swing.JButton();
        BButton = new javax.swing.JButton();
        NButton = new javax.swing.JButton();
        OButton = new javax.swing.JButton();
        PButton = new javax.swing.JButton();
        AButton = new javax.swing.JButton();
        SButton = new javax.swing.JButton();
        DButton = new javax.swing.JButton();
        FButton = new javax.swing.JButton();
        GButton = new javax.swing.JButton();
        HButton = new javax.swing.JButton();
        JButton = new javax.swing.JButton();
        LButton = new javax.swing.JButton();
        KButton = new javax.swing.JButton();
        YButton = new javax.swing.JButton();
        RButton = new javax.swing.JButton();
        TButton = new javax.swing.JButton();
        WButton = new javax.swing.JButton();
        EButton = new javax.swing.JButton();
        QButton = new javax.swing.JButton();
        IButton = new javax.swing.JButton();
        UButton = new javax.swing.JButton();
        PrevLocMapping = new javax.swing.JButton();
        NextLocMapping = new javax.swing.JButton();
        LocMappingNumberLabel = new javax.swing.JLabel();
        QSlider = new javax.swing.JSlider();
        WSlider = new javax.swing.JSlider();
        ESlider = new javax.swing.JSlider();
        RSlider = new javax.swing.JSlider();
        TSlider = new javax.swing.JSlider();
        YSlider = new javax.swing.JSlider();
        USlider = new javax.swing.JSlider();
        OSlider = new javax.swing.JSlider();
        PSlider = new javax.swing.JSlider();
        ISlider = new javax.swing.JSlider();
        ASlider = new javax.swing.JSlider();
        SSlider = new javax.swing.JSlider();
        DSlider = new javax.swing.JSlider();
        FSlider = new javax.swing.JSlider();
        GSlider = new javax.swing.JSlider();
        HSlider = new javax.swing.JSlider();
        JSlider = new javax.swing.JSlider();
        KSlider = new javax.swing.JSlider();
        LSlider = new javax.swing.JSlider();
        MSlider = new javax.swing.JSlider();
        NSlider = new javax.swing.JSlider();
        BSlider = new javax.swing.JSlider();
        VSlider = new javax.swing.JSlider();
        CSlider = new javax.swing.JSlider();
        XSlider = new javax.swing.JSlider();
        ZSlider = new javax.swing.JSlider();
        ELabel = new javax.swing.JTextField();
        QLabel = new javax.swing.JTextField();
        WLabel = new javax.swing.JTextField();
        RLabel = new javax.swing.JTextField();
        TLabel = new javax.swing.JTextField();
        YLabel = new javax.swing.JTextField();
        ULabel = new javax.swing.JTextField();
        ILabel = new javax.swing.JTextField();
        OLabel = new javax.swing.JTextField();
        PLabel = new javax.swing.JTextField();
        ALabel = new javax.swing.JTextField();
        SLabel = new javax.swing.JTextField();
        ZLabel = new javax.swing.JTextField();
        DLabel = new javax.swing.JTextField();
        FLabel = new javax.swing.JTextField();
        GLabel = new javax.swing.JTextField();
        HLabel = new javax.swing.JTextField();
        JLabel = new javax.swing.JTextField();
        KLabel = new javax.swing.JTextField();
        LLabel = new javax.swing.JTextField();
        XLabel = new javax.swing.JTextField();
        CLabel = new javax.swing.JTextField();
        VLabel = new javax.swing.JTextField();
        BLabel = new javax.swing.JTextField();
        NLabel = new javax.swing.JTextField();
        MLabel = new javax.swing.JTextField();
        controlsPanel = new javax.swing.JPanel();
        UpArrow = new javax.swing.JButton();
        DownArrow = new javax.swing.JButton();
        RightArrow = new javax.swing.JButton();
        LeftArrow = new javax.swing.JButton();
        SpacebarButton = new javax.swing.JButton();
        SlowStopLabel = new javax.swing.JLabel();
        EStopLabel = new javax.swing.JLabel();
        ShiftButton = new javax.swing.JButton();
        DirectionLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        AltEmergencyStop = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        ZeroButton = new javax.swing.JButton();
        FullSpeedLabel = new javax.swing.JLabel();
        EightButton = new javax.swing.JButton();
        NineButton = new javax.swing.JButton();
        SevenButton = new javax.swing.JButton();
        FourButton = new javax.swing.JButton();
        ThreeButton = new javax.swing.JButton();
        TwoButton = new javax.swing.JButton();
        OneButton = new javax.swing.JButton();
        SixButton = new javax.swing.JButton();
        FiveButton = new javax.swing.JButton();
        ZeroPercentSpeedLabel = new javax.swing.JLabel();
        PrimaryControls = new javax.swing.JLabel();
        latencyLabel = new javax.swing.JLabel();
        toggleMenuBar = new javax.swing.JCheckBox();
        layoutPanel = new javax.swing.JPanel();
        LayoutList = new javax.swing.JComboBox();
        layoutListLabel = new javax.swing.JLabel();
        LayoutArea = new javax.swing.JScrollPane();
        InnerLayoutPanel = new javax.swing.JPanel();
        sizeLabel = new javax.swing.JLabel();
        SizeList = new javax.swing.JComboBox();
        layoutNewWindow = new javax.swing.JButton();
        smallButton = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        allButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        editLayoutButton = new javax.swing.JButton();
        autoPanel = new javax.swing.JPanel();
        locCommandPanels = new javax.swing.JTabbedPane();
        autonomyPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        autonomyJSON = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        exportJSON = new javax.swing.JButton();
        loadJSONButton = new javax.swing.JButton();
        autosave = new javax.swing.JCheckBox();
        jsonDocumentationButton = new javax.swing.JButton();
        loadDefaultBlankGraph = new javax.swing.JButton();
        validateButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        locCommandTab = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        autoLocPanel = new javax.swing.JPanel();
        gracefulStop = new javax.swing.JButton();
        startAutonomy = new javax.swing.JButton();
        timetablePanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        timetable = new javax.swing.JTable();
        executeTimetable = new javax.swing.JButton();
        timetableCapture = new javax.swing.JToggleButton();
        autoSettingsPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        minDelay = new javax.swing.JSlider();
        jLabel48 = new javax.swing.JLabel();
        maxLocInactiveSeconds = new javax.swing.JSlider();
        jLabel47 = new javax.swing.JLabel();
        maxDelay = new javax.swing.JSlider();
        jLabel43 = new javax.swing.JLabel();
        defaultLocSpeed = new javax.swing.JSlider();
        jLabel49 = new javax.swing.JLabel();
        preArrivalSpeedReduction = new javax.swing.JSlider();
        jLabel50 = new javax.swing.JLabel();
        atomicRoutes = new javax.swing.JCheckBox();
        turnOffFunctionsOnArrival = new javax.swing.JCheckBox();
        simulate = new javax.swing.JCheckBox();
        turnOnFunctionsOnDeparture = new javax.swing.JCheckBox();
        maximumLatency = new javax.swing.JSlider();
        jLabel44 = new javax.swing.JLabel();
        maxActiveTrains = new javax.swing.JSlider();
        jLabel53 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        hideReversing = new javax.swing.JCheckBox();
        hideInactive = new javax.swing.JCheckBox();
        showStationLengths = new javax.swing.JCheckBox();
        jLabel52 = new javax.swing.JLabel();
        KeyboardPanel = new javax.swing.JPanel();
        KeyboardLabel = new javax.swing.JLabel();
        keyboardButtonPanel = new javax.swing.JPanel();
        SwitchButton1 = new javax.swing.JToggleButton();
        SwitchButton2 = new javax.swing.JToggleButton();
        SwitchButton3 = new javax.swing.JToggleButton();
        SwitchButton4 = new javax.swing.JToggleButton();
        SwitchButton5 = new javax.swing.JToggleButton();
        SwitchButton6 = new javax.swing.JToggleButton();
        SwitchButton7 = new javax.swing.JToggleButton();
        SwitchButton8 = new javax.swing.JToggleButton();
        SwitchButton9 = new javax.swing.JToggleButton();
        SwitchButton10 = new javax.swing.JToggleButton();
        SwitchButton11 = new javax.swing.JToggleButton();
        SwitchButton12 = new javax.swing.JToggleButton();
        SwitchButton13 = new javax.swing.JToggleButton();
        SwitchButton14 = new javax.swing.JToggleButton();
        SwitchButton15 = new javax.swing.JToggleButton();
        SwitchButton16 = new javax.swing.JToggleButton();
        SwitchButton17 = new javax.swing.JToggleButton();
        SwitchButton18 = new javax.swing.JToggleButton();
        SwitchButton19 = new javax.swing.JToggleButton();
        SwitchButton21 = new javax.swing.JToggleButton();
        SwitchButton22 = new javax.swing.JToggleButton();
        SwitchButton23 = new javax.swing.JToggleButton();
        SwitchButton24 = new javax.swing.JToggleButton();
        SwitchButton26 = new javax.swing.JToggleButton();
        SwitchButton27 = new javax.swing.JToggleButton();
        SwitchButton29 = new javax.swing.JToggleButton();
        SwitchButton28 = new javax.swing.JToggleButton();
        SwitchButton30 = new javax.swing.JToggleButton();
        SwitchButton31 = new javax.swing.JToggleButton();
        SwitchButton32 = new javax.swing.JToggleButton();
        SwitchButton33 = new javax.swing.JToggleButton();
        SwitchButton34 = new javax.swing.JToggleButton();
        SwitchButton35 = new javax.swing.JToggleButton();
        SwitchButton36 = new javax.swing.JToggleButton();
        SwitchButton37 = new javax.swing.JToggleButton();
        SwitchButton38 = new javax.swing.JToggleButton();
        SwitchButton39 = new javax.swing.JToggleButton();
        SwitchButton40 = new javax.swing.JToggleButton();
        SwitchButton41 = new javax.swing.JToggleButton();
        SwitchButton42 = new javax.swing.JToggleButton();
        SwitchButton43 = new javax.swing.JToggleButton();
        SwitchButton44 = new javax.swing.JToggleButton();
        SwitchButton45 = new javax.swing.JToggleButton();
        SwitchButton46 = new javax.swing.JToggleButton();
        SwitchButton47 = new javax.swing.JToggleButton();
        SwitchButton48 = new javax.swing.JToggleButton();
        SwitchButton49 = new javax.swing.JToggleButton();
        SwitchButton50 = new javax.swing.JToggleButton();
        SwitchButton51 = new javax.swing.JToggleButton();
        SwitchButton52 = new javax.swing.JToggleButton();
        SwitchButton53 = new javax.swing.JToggleButton();
        SwitchButton54 = new javax.swing.JToggleButton();
        SwitchButton55 = new javax.swing.JToggleButton();
        SwitchButton57 = new javax.swing.JToggleButton();
        SwitchButton58 = new javax.swing.JToggleButton();
        SwitchButton59 = new javax.swing.JToggleButton();
        SwitchButton60 = new javax.swing.JToggleButton();
        SwitchButton61 = new javax.swing.JToggleButton();
        SwitchButton62 = new javax.swing.JToggleButton();
        SwitchButton63 = new javax.swing.JToggleButton();
        SwitchButton20 = new javax.swing.JToggleButton();
        SwitchButton56 = new javax.swing.JToggleButton();
        SwitchButton25 = new javax.swing.JToggleButton();
        SwitchButton64 = new javax.swing.JToggleButton();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jSeparator12 = new javax.swing.JSeparator();
        jSeparator13 = new javax.swing.JSeparator();
        jSeparator14 = new javax.swing.JSeparator();
        jSeparator15 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        PrevKeyboard = new javax.swing.JButton();
        KeyboardNumberLabel = new javax.swing.JLabel();
        NextKeyboard = new javax.swing.JButton();
        KeyboardLabel1 = new javax.swing.JLabel();
        MM2 = new javax.swing.JRadioButton();
        DCC = new javax.swing.JRadioButton();
        RoutePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        RouteList = new javax.swing.JTable();
        AddRouteButton = new javax.swing.JButton();
        sortByName = new javax.swing.JRadioButton();
        sortByID = new javax.swing.JRadioButton();
        BulkEnable = new javax.swing.JButton();
        BulkDisable = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        logPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        debugArea = new javax.swing.JTextArea();
        LocFunctionsPanel = new javax.swing.JPanel();
        OnButton = new javax.swing.JButton();
        PowerOff = new javax.swing.JButton();
        ActiveLocLabel = new javax.swing.JLabel();
        SpeedSlider = new javax.swing.JSlider();
        Backward = new javax.swing.JToggleButton();
        Forward = new javax.swing.JToggleButton();
        CurrentKeyLabel = new javax.swing.JLabel();
        locIcon = new javax.swing.JLabel();
        FunctionTabs = new javax.swing.JTabbedPane();
        functionPanel = new javax.swing.JPanel();
        F8 = new javax.swing.JToggleButton();
        F9 = new javax.swing.JToggleButton();
        F7 = new javax.swing.JToggleButton();
        F10 = new javax.swing.JToggleButton();
        F11 = new javax.swing.JToggleButton();
        F0 = new javax.swing.JToggleButton();
        F3 = new javax.swing.JToggleButton();
        F1 = new javax.swing.JToggleButton();
        F2 = new javax.swing.JToggleButton();
        F4 = new javax.swing.JToggleButton();
        F5 = new javax.swing.JToggleButton();
        F6 = new javax.swing.JToggleButton();
        f0Label = new javax.swing.JLabel();
        f4Label = new javax.swing.JLabel();
        f8Label = new javax.swing.JLabel();
        f6Label = new javax.swing.JLabel();
        f1Label = new javax.swing.JLabel();
        f3Label = new javax.swing.JLabel();
        f5Label = new javax.swing.JLabel();
        f7Label = new javax.swing.JLabel();
        f10Label = new javax.swing.JLabel();
        f9Label = new javax.swing.JLabel();
        f11Label = new javax.swing.JLabel();
        f2Label = new javax.swing.JLabel();
        f12Label = new javax.swing.JLabel();
        F12 = new javax.swing.JToggleButton();
        f13Label = new javax.swing.JLabel();
        F13 = new javax.swing.JToggleButton();
        f14Label = new javax.swing.JLabel();
        F14 = new javax.swing.JToggleButton();
        f15Label = new javax.swing.JLabel();
        F15 = new javax.swing.JToggleButton();
        F16 = new javax.swing.JToggleButton();
        F17 = new javax.swing.JToggleButton();
        F18 = new javax.swing.JToggleButton();
        f16Label = new javax.swing.JLabel();
        f17Label = new javax.swing.JLabel();
        f18Label = new javax.swing.JLabel();
        F19 = new javax.swing.JToggleButton();
        f19Label = new javax.swing.JLabel();
        F20AndUpPanel = new javax.swing.JPanel();
        f23Label = new javax.swing.JLabel();
        f20Label = new javax.swing.JLabel();
        F22 = new javax.swing.JToggleButton();
        F23 = new javax.swing.JToggleButton();
        f22Label = new javax.swing.JLabel();
        f21Label = new javax.swing.JLabel();
        F20 = new javax.swing.JToggleButton();
        F21 = new javax.swing.JToggleButton();
        f27Label = new javax.swing.JLabel();
        f29Label = new javax.swing.JLabel();
        F24 = new javax.swing.JToggleButton();
        f30Label = new javax.swing.JLabel();
        F27 = new javax.swing.JToggleButton();
        f31Label = new javax.swing.JLabel();
        f24Label = new javax.swing.JLabel();
        F25 = new javax.swing.JToggleButton();
        F26 = new javax.swing.JToggleButton();
        F28 = new javax.swing.JToggleButton();
        f28Label = new javax.swing.JLabel();
        F29 = new javax.swing.JToggleButton();
        f26Label = new javax.swing.JLabel();
        F30 = new javax.swing.JToggleButton();
        f25Label = new javax.swing.JLabel();
        F31 = new javax.swing.JToggleButton();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        backupDataMenuItem = new javax.swing.JMenuItem();
        changeIPMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        locomotiveMenu = new javax.swing.JMenu();
        quickFindMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        viewDatabaseMenuItem = new javax.swing.JMenuItem();
        addLocomotiveMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        syncMenuItem = new javax.swing.JMenuItem();
        functionsMenu = new javax.swing.JMenu();
        turnOnLightsMenuItem = new javax.swing.JMenuItem();
        turnOffFunctionsMenuItem = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        syncFullLocStateMenuItem = new javax.swing.JMenuItem();
        layoutMenu = new javax.swing.JMenu();
        showCurrentLayoutFolderMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        chooseLocalDataFolderMenuItem = new javax.swing.JMenuItem();
        modifyLocalLayoutMenu = new javax.swing.JMenu();
        addBlankPageMenuItem = new javax.swing.JMenuItem();
        renameLayoutMenuItem = new javax.swing.JMenuItem();
        duplicateLayoutMenuItem = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JPopupMenu.Separator();
        editCurrentPageActionPerformed = new javax.swing.JMenuItem();
        openLegacyTrackDiagramEditor = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JPopupMenu.Separator();
        deleteLayoutMenuItem = new javax.swing.JMenuItem();
        initializeLocalLayoutMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        switchCSLayoutMenuItem = new javax.swing.JMenuItem();
        downloadCSLayoutMenuItem = new javax.swing.JMenuItem();
        openCS3AppMenuItem = new javax.swing.JMenuItem();
        routesMenu = new javax.swing.JMenu();
        exportRoutesMenuItem = new javax.swing.JMenuItem();
        importRoutesMenuItem = new javax.swing.JMenuItem();
        interfaceMenu = new javax.swing.JMenu();
        windowAlwaysOnTopMenuItem = new javax.swing.JCheckBoxMenuItem();
        rememberLocationMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        locomotiveControlMenu = new javax.swing.JMenu();
        slidersChangeActiveLocMenuItem = new javax.swing.JCheckBoxMenuItem();
        showKeyboardHintsMenuItem = new javax.swing.JCheckBoxMenuItem();
        activeLocInTitle = new javax.swing.JCheckBoxMenuItem();
        jSeparator20 = new javax.swing.JPopupMenu.Separator();
        keyboardQwertyMenuItem = new javax.swing.JRadioButtonMenuItem();
        keyboardQwertzMenuItem = new javax.swing.JRadioButtonMenuItem();
        keyboardAzertyMenuItem = new javax.swing.JRadioButtonMenuItem();
        jMenu1 = new javax.swing.JMenu();
        powerOnStartup = new javax.swing.JRadioButtonMenuItem();
        powerOffStartup = new javax.swing.JRadioButtonMenuItem();
        powerNoChangeStartup = new javax.swing.JRadioButtonMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        AutoLoadAutonomyMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        checkForUpdates = new javax.swing.JCheckBoxMenuItem();
        layoutMenuItem = new javax.swing.JMenu();
        menuItemShowLayoutAddresses = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        viewReleasesMenuItem = new javax.swing.JMenuItem();
        downloadUpdateMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(MarklinControlStation.PROG_TITLE + MarklinControlStation.VERSION);
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(255, 255, 255));
        setFocusable(false);
        setIconImage(Toolkit.getDefaultToolkit().getImage(TrainControlUI.class.getResource("resources/locicon.png")));
        setMinimumSize(new java.awt.Dimension(1110, 619));
        setResizable(false);
        setSize(new java.awt.Dimension(1110, 619));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                WindowClosed(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                LocControlPanelKeyPressed(evt);
            }
        });

        KeyboardTab.setBackground(new java.awt.Color(255, 255, 255));
        KeyboardTab.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        KeyboardTab.setToolTipText(null);
        KeyboardTab.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        KeyboardTab.setMaximumSize(new java.awt.Dimension(802, 589));
        KeyboardTab.setMinimumSize(new java.awt.Dimension(802, 589));
        KeyboardTab.setPreferredSize(new java.awt.Dimension(802, 589));
        KeyboardTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                KeyboardTabStateChanged(evt);
            }
        });
        KeyboardTab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LocControlPanelKeyPressed(evt);
            }
        });

        LocControlPanel.setBackground(new java.awt.Color(255, 255, 255));
        LocControlPanel.setToolTipText(null);
        LocControlPanel.setMaximumSize(new java.awt.Dimension(806, 589));
        LocControlPanel.setMinimumSize(new java.awt.Dimension(806, 589));
        LocControlPanel.setPreferredSize(new java.awt.Dimension(806, 589));
        LocControlPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LocControlPanelKeyPressed(evt);
            }
        });

        locMappingLabel.setFont(new java.awt.Font("Segoe UI Semibold", 1, 13)); // NOI18N
        locMappingLabel.setForeground(new java.awt.Color(0, 0, 155));
        locMappingLabel.setText("Locomotive Key Mapping");
        locMappingLabel.setToolTipText("Right-click any button for options");

        LocContainer.setBackground(new java.awt.Color(245, 245, 245));
        LocContainer.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        LocContainer.setMaximumSize(new java.awt.Dimension(773, 366));
        LocContainer.setMinimumSize(new java.awt.Dimension(773, 366));

        CButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        CButton.setText("C");
        CButton.setFocusable(false);
        CButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        VButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        VButton.setText("V");
        VButton.setFocusable(false);
        VButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        ZButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        ZButton.setText("Z");
        ZButton.setFocusable(false);
        ZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        XButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        XButton.setText("X");
        XButton.setFocusable(false);
        XButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        MButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        MButton.setText("M");
        MButton.setFocusable(false);
        MButton.setMaximumSize(new java.awt.Dimension(39, 23));
        MButton.setMinimumSize(new java.awt.Dimension(39, 23));
        MButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        BButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BButton.setText("B");
        BButton.setFocusable(false);
        BButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        NButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        NButton.setText("N");
        NButton.setFocusable(false);
        NButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        OButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        OButton.setText("O");
        OButton.setFocusable(false);
        OButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        PButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        PButton.setText("P");
        PButton.setFocusable(false);
        PButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        AButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        AButton.setText("A");
        AButton.setFocusable(false);
        AButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        SButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        SButton.setText("S");
        SButton.setFocusable(false);
        SButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        DButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        DButton.setText("D");
        DButton.setFocusable(false);
        DButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        FButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        FButton.setText("F");
        FButton.setFocusable(false);
        FButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        GButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        GButton.setText("G");
        GButton.setFocusable(false);
        GButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        HButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        HButton.setText("H");
        HButton.setFocusable(false);
        HButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        JButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        JButton.setText("J");
        JButton.setFocusable(false);
        JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        LButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        LButton.setText("L");
        LButton.setFocusable(false);
        LButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        KButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        KButton.setText("K");
        KButton.setFocusable(false);
        KButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        YButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        YButton.setText("Y");
        YButton.setFocusable(false);
        YButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        RButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        RButton.setText("R");
        RButton.setFocusable(false);
        RButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        TButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        TButton.setText("T");
        TButton.setFocusable(false);
        TButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        WButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        WButton.setText("W");
        WButton.setFocusable(false);
        WButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        EButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        EButton.setText("E");
        EButton.setFocusable(false);
        EButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        QButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        QButton.setText("Q");
        QButton.setFocusable(false);
        QButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        IButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        IButton.setText("I");
        IButton.setFocusable(false);
        IButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        UButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        UButton.setText("U");
        UButton.setFocusable(false);
        UButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LetterButtonPressed(evt);
            }
        });

        PrevLocMapping.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        PrevLocMapping.setText("<< ,");
        PrevLocMapping.setFocusable(false);
        PrevLocMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevLocMappingActionPerformed(evt);
            }
        });

        NextLocMapping.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NextLocMapping.setText(". >>");
        NextLocMapping.setFocusable(false);
        NextLocMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextLocMappingActionPerformed(evt);
            }
        });

        LocMappingNumberLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        LocMappingNumberLabel.setText("Page");
        LocMappingNumberLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        LocMappingNumberLabel.setFocusable(false);
        LocMappingNumberLabel.setMaximumSize(new java.awt.Dimension(135, 18));

        QSlider.setMajorTickSpacing(10);
        QSlider.setMinorTickSpacing(5);
        QSlider.setFocusable(false);
        QSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        QSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        QSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        QSlider.setRequestFocusEnabled(false);
        QSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        WSlider.setFocusable(false);
        WSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        WSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        WSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        WSlider.setRequestFocusEnabled(false);
        WSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        ESlider.setFocusable(false);
        ESlider.setMaximumSize(new java.awt.Dimension(60, 26));
        ESlider.setMinimumSize(new java.awt.Dimension(60, 26));
        ESlider.setPreferredSize(new java.awt.Dimension(60, 26));
        ESlider.setRequestFocusEnabled(false);
        ESlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        RSlider.setFocusable(false);
        RSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        RSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        RSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        RSlider.setRequestFocusEnabled(false);
        RSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        TSlider.setFocusable(false);
        TSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        TSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        TSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        TSlider.setRequestFocusEnabled(false);
        TSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        YSlider.setFocusable(false);
        YSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        YSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        YSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        YSlider.setRequestFocusEnabled(false);
        YSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        USlider.setFocusable(false);
        USlider.setMaximumSize(new java.awt.Dimension(60, 26));
        USlider.setMinimumSize(new java.awt.Dimension(60, 26));
        USlider.setPreferredSize(new java.awt.Dimension(60, 26));
        USlider.setRequestFocusEnabled(false);
        USlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        OSlider.setFocusable(false);
        OSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        OSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        OSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        OSlider.setRequestFocusEnabled(false);
        OSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        PSlider.setFocusable(false);
        PSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        PSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        PSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        PSlider.setRequestFocusEnabled(false);
        PSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        ISlider.setFocusable(false);
        ISlider.setMaximumSize(new java.awt.Dimension(60, 26));
        ISlider.setMinimumSize(new java.awt.Dimension(60, 26));
        ISlider.setPreferredSize(new java.awt.Dimension(60, 26));
        ISlider.setRequestFocusEnabled(false);
        ISlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        ASlider.setFocusable(false);
        ASlider.setMaximumSize(new java.awt.Dimension(60, 26));
        ASlider.setMinimumSize(new java.awt.Dimension(60, 26));
        ASlider.setPreferredSize(new java.awt.Dimension(60, 26));
        ASlider.setRequestFocusEnabled(false);
        ASlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        SSlider.setFocusable(false);
        SSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        SSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        SSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        SSlider.setRequestFocusEnabled(false);
        SSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        DSlider.setFocusable(false);
        DSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        DSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        DSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        DSlider.setRequestFocusEnabled(false);
        DSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        FSlider.setFocusable(false);
        FSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        FSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        FSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        FSlider.setRequestFocusEnabled(false);
        FSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        GSlider.setFocusable(false);
        GSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        GSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        GSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        GSlider.setRequestFocusEnabled(false);
        GSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        HSlider.setFocusable(false);
        HSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        HSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        HSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        HSlider.setRequestFocusEnabled(false);
        HSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        JSlider.setFocusable(false);
        JSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        JSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        JSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        JSlider.setRequestFocusEnabled(false);
        JSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        KSlider.setFocusable(false);
        KSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        KSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        KSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        KSlider.setRequestFocusEnabled(false);
        KSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        LSlider.setFocusable(false);
        LSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        LSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        LSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        LSlider.setRequestFocusEnabled(false);
        LSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        MSlider.setFocusable(false);
        MSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        MSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        MSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        MSlider.setRequestFocusEnabled(false);
        MSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        NSlider.setFocusable(false);
        NSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        NSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        NSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        NSlider.setRequestFocusEnabled(false);
        NSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        BSlider.setFocusable(false);
        BSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        BSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        BSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        BSlider.setRequestFocusEnabled(false);
        BSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        VSlider.setFocusable(false);
        VSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        VSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        VSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        VSlider.setRequestFocusEnabled(false);
        VSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        CSlider.setFocusable(false);
        CSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        CSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        CSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        CSlider.setRequestFocusEnabled(false);
        CSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        XSlider.setFocusable(false);
        XSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        XSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        XSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        XSlider.setRequestFocusEnabled(false);
        XSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        ZSlider.setFocusable(false);
        ZSlider.setMaximumSize(new java.awt.Dimension(60, 26));
        ZSlider.setMinimumSize(new java.awt.Dimension(60, 26));
        ZSlider.setPreferredSize(new java.awt.Dimension(60, 26));
        ZSlider.setRequestFocusEnabled(false);
        ZSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                updateSliderSpeed(evt);
            }
        });

        ELabel.setBackground(new java.awt.Color(245, 245, 245));
        ELabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        ELabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        ELabel.setText("label");
        ELabel.setAutoscrolls(false);
        ELabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ELabel.setCaretPosition(0);
        ELabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ELabel.setFocusable(false);
        ELabel.setMaximumSize(new java.awt.Dimension(64, 21));
        ELabel.setMinimumSize(new java.awt.Dimension(64, 21));

        QLabel.setBackground(new java.awt.Color(245, 245, 245));
        QLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        QLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        QLabel.setText("label");
        QLabel.setAutoscrolls(false);
        QLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        QLabel.setCaretPosition(0);
        QLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        QLabel.setFocusable(false);
        QLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        QLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        WLabel.setBackground(new java.awt.Color(245, 245, 245));
        WLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        WLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        WLabel.setText("label");
        WLabel.setAutoscrolls(false);
        WLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        WLabel.setCaretPosition(0);
        WLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        WLabel.setFocusable(false);
        WLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        WLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        RLabel.setBackground(new java.awt.Color(245, 245, 245));
        RLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        RLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        RLabel.setText("label");
        RLabel.setAutoscrolls(false);
        RLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        RLabel.setCaretPosition(0);
        RLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        RLabel.setFocusable(false);
        RLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        RLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        TLabel.setBackground(new java.awt.Color(245, 245, 245));
        TLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        TLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        TLabel.setText("label");
        TLabel.setAutoscrolls(false);
        TLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TLabel.setCaretPosition(0);
        TLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        TLabel.setFocusable(false);
        TLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        TLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        YLabel.setBackground(new java.awt.Color(245, 245, 245));
        YLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        YLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        YLabel.setText("label");
        YLabel.setAutoscrolls(false);
        YLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        YLabel.setCaretPosition(0);
        YLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        YLabel.setFocusable(false);
        YLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        YLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        ULabel.setBackground(new java.awt.Color(245, 245, 245));
        ULabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        ULabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        ULabel.setText("label");
        ULabel.setAutoscrolls(false);
        ULabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ULabel.setCaretPosition(0);
        ULabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ULabel.setFocusable(false);
        ULabel.setMaximumSize(new java.awt.Dimension(64, 21));
        ULabel.setMinimumSize(new java.awt.Dimension(64, 21));

        ILabel.setBackground(new java.awt.Color(245, 245, 245));
        ILabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        ILabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        ILabel.setText("label");
        ILabel.setAutoscrolls(false);
        ILabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ILabel.setCaretPosition(0);
        ILabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ILabel.setFocusable(false);
        ILabel.setMaximumSize(new java.awt.Dimension(64, 21));
        ILabel.setMinimumSize(new java.awt.Dimension(64, 21));

        OLabel.setBackground(new java.awt.Color(245, 245, 245));
        OLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        OLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        OLabel.setText("label");
        OLabel.setAutoscrolls(false);
        OLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        OLabel.setCaretPosition(0);
        OLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        OLabel.setFocusable(false);
        OLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        OLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        PLabel.setBackground(new java.awt.Color(245, 245, 245));
        PLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        PLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        PLabel.setText("label");
        PLabel.setAutoscrolls(false);
        PLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        PLabel.setCaretPosition(0);
        PLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        PLabel.setFocusable(false);
        PLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        PLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        ALabel.setBackground(new java.awt.Color(245, 245, 245));
        ALabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        ALabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        ALabel.setText("label");
        ALabel.setAutoscrolls(false);
        ALabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ALabel.setCaretPosition(0);
        ALabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ALabel.setFocusable(false);
        ALabel.setMaximumSize(new java.awt.Dimension(64, 21));
        ALabel.setMinimumSize(new java.awt.Dimension(64, 21));

        SLabel.setBackground(new java.awt.Color(245, 245, 245));
        SLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        SLabel.setText("label");
        SLabel.setAutoscrolls(false);
        SLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        SLabel.setCaretPosition(0);
        SLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        SLabel.setFocusable(false);
        SLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        SLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        ZLabel.setBackground(new java.awt.Color(245, 245, 245));
        ZLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        ZLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        ZLabel.setText("label");
        ZLabel.setAutoscrolls(false);
        ZLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ZLabel.setCaretPosition(0);
        ZLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        ZLabel.setFocusable(false);
        ZLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        ZLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        DLabel.setBackground(new java.awt.Color(245, 245, 245));
        DLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        DLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        DLabel.setText("label");
        DLabel.setAutoscrolls(false);
        DLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        DLabel.setCaretPosition(0);
        DLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        DLabel.setFocusable(false);
        DLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        DLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        FLabel.setBackground(new java.awt.Color(245, 245, 245));
        FLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        FLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        FLabel.setText("label");
        FLabel.setAutoscrolls(false);
        FLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        FLabel.setCaretPosition(0);
        FLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        FLabel.setFocusable(false);
        FLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        FLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        GLabel.setBackground(new java.awt.Color(245, 245, 245));
        GLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        GLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        GLabel.setText("label");
        GLabel.setAutoscrolls(false);
        GLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        GLabel.setCaretPosition(0);
        GLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        GLabel.setFocusable(false);
        GLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        GLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        HLabel.setBackground(new java.awt.Color(245, 245, 245));
        HLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        HLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        HLabel.setText("label");
        HLabel.setAutoscrolls(false);
        HLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        HLabel.setCaretPosition(0);
        HLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        HLabel.setFocusable(false);
        HLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        HLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        JLabel.setBackground(new java.awt.Color(245, 245, 245));
        JLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        JLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        JLabel.setText("label");
        JLabel.setAutoscrolls(false);
        JLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        JLabel.setCaretPosition(0);
        JLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        JLabel.setFocusable(false);
        JLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        JLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        KLabel.setBackground(new java.awt.Color(245, 245, 245));
        KLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        KLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        KLabel.setText("label");
        KLabel.setAutoscrolls(false);
        KLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        KLabel.setCaretPosition(0);
        KLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        KLabel.setFocusable(false);
        KLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        KLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        LLabel.setBackground(new java.awt.Color(245, 245, 245));
        LLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        LLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        LLabel.setText("label");
        LLabel.setAutoscrolls(false);
        LLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        LLabel.setCaretPosition(0);
        LLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        LLabel.setFocusable(false);
        LLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        LLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        XLabel.setBackground(new java.awt.Color(245, 245, 245));
        XLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        XLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        XLabel.setText("label");
        XLabel.setAutoscrolls(false);
        XLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        XLabel.setCaretPosition(0);
        XLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        XLabel.setFocusable(false);
        XLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        XLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        CLabel.setBackground(new java.awt.Color(245, 245, 245));
        CLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        CLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        CLabel.setText("label");
        CLabel.setAutoscrolls(false);
        CLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        CLabel.setCaretPosition(0);
        CLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        CLabel.setFocusable(false);
        CLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        CLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        VLabel.setBackground(new java.awt.Color(245, 245, 245));
        VLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        VLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        VLabel.setText("label");
        VLabel.setAutoscrolls(false);
        VLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        VLabel.setCaretPosition(0);
        VLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        VLabel.setFocusable(false);
        VLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        VLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        BLabel.setBackground(new java.awt.Color(245, 245, 245));
        BLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        BLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        BLabel.setText("label");
        BLabel.setAutoscrolls(false);
        BLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        BLabel.setCaretPosition(0);
        BLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        BLabel.setFocusable(false);
        BLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        BLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        NLabel.setBackground(new java.awt.Color(245, 245, 245));
        NLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        NLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        NLabel.setText("label");
        NLabel.setAutoscrolls(false);
        NLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        NLabel.setCaretPosition(0);
        NLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        NLabel.setFocusable(false);
        NLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        NLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        MLabel.setBackground(new java.awt.Color(245, 245, 245));
        MLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        MLabel.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        MLabel.setText("label");
        MLabel.setAutoscrolls(false);
        MLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        MLabel.setCaretPosition(0);
        MLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        MLabel.setFocusable(false);
        MLabel.setMaximumSize(new java.awt.Dimension(64, 21));
        MLabel.setMinimumSize(new java.awt.Dimension(64, 21));

        javax.swing.GroupLayout LocContainerLayout = new javax.swing.GroupLayout(LocContainer);
        LocContainer.setLayout(LocContainerLayout);
        LocContainerLayout.setHorizontalGroup(
            LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LocContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LocContainerLayout.createSequentialGroup()
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(QButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(QSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(QLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(WButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(WSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(WLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(EButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(ESlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(ELabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(RSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(RButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(RLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(TSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(TLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(YSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(YButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(YLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(USlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(UButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ULabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(IButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(ISlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ILabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(OSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(OButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(OLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(PButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(PSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(PLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(LocContainerLayout.createSequentialGroup()
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(ASlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(AButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(SSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(SButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(SLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(DButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(DSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(DLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(FSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(FButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(FLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(GSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(GButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(GLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(HSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(HButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(HLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(JSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(JButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(JLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(KSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(KButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(KLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createSequentialGroup()
                                .addComponent(PrevLocMapping, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(NextLocMapping, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(LocContainerLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(LocMappingNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(LButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(LSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(LLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(LocContainerLayout.createSequentialGroup()
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ZSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ZButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ZLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(XSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(XButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(XLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(CSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(CButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(CLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(VSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(VButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(VLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(BSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(BButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(BLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(NSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(NButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(NLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        LocContainerLayout.setVerticalGroup(
            LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LocContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(WButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(EButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(YButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(UButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(QButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(IButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(QSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(WSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ESlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(RSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(YSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(OSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(PSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ISlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(USlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ELabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(QLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(WLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(YLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ULabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ILabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(GButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(KButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(ASlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(DSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(FSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(HSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(GSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(JSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(KSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(GLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(HLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(KLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(LocContainerLayout.createSequentialGroup()
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                            .addComponent(ZButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(XButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(VButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(ZSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(XSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(CSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(VSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(NSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(BSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(MSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ZLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(XLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(VLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(MLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(LocContainerLayout.createSequentialGroup()
                        .addComponent(LocMappingNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(LocContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(PrevLocMapping)
                            .addComponent(NextLocMapping))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        controlsPanel.setBackground(new java.awt.Color(245, 245, 245));
        controlsPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        UpArrow.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        UpArrow.setText("↑");
        UpArrow.setToolTipText("Increase Speed (+Control to fine-tune, +Alt for 2x increment)");
        UpArrow.setFocusable(false);
        UpArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpArrowLetterButtonPressed(evt);
            }
        });

        DownArrow.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        DownArrow.setText("↓");
        DownArrow.setToolTipText("Decrease Speed (+Control to fine-tune, +Alt for 2x increment)");
        DownArrow.setFocusable(false);
        DownArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DownArrowLetterButtonPressed(evt);
            }
        });

        RightArrow.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        RightArrow.setText("→");
        RightArrow.setToolTipText("Switch Direction (+Control to force forward)");
        RightArrow.setFocusable(false);
        RightArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RightArrowLetterButtonPressed(evt);
            }
        });

        LeftArrow.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        LeftArrow.setText("←");
        LeftArrow.setToolTipText("Switch Direction (+Control to force reverse)");
        LeftArrow.setFocusable(false);
        LeftArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LeftArrowLetterButtonPressed(evt);
            }
        });

        SpacebarButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        SpacebarButton.setText("Spacebar");
        SpacebarButton.setToolTipText("Spacebar: emergency stop of current locomotive.");
        SpacebarButton.setFocusable(false);
        SpacebarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SpacebarButtonActionPerformed(evt);
            }
        });

        SlowStopLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        SlowStopLabel.setText("Slow Stop");

        EStopLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        EStopLabel.setText("Stop All");

        ShiftButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ShiftButton.setText("Shift");
        ShiftButton.setToolTipText("Shift: stop locomotive");
        ShiftButton.setFocusable(false);
        ShiftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShiftButtonActionPerformed(evt);
            }
        });

        DirectionLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        DirectionLabel.setText("Direction");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Increment Speed");

        AltEmergencyStop.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        AltEmergencyStop.setText("Enter");
        AltEmergencyStop.setToolTipText("Enter: stops all running locomotives.");
        AltEmergencyStop.setFocusable(false);
        AltEmergencyStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AltEmergencyStopActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Instant Stop");

        ZeroButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ZeroButton.setText("0");
        ZeroButton.setFocusable(false);
        ZeroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZeroButtonActionPerformed(evt);
            }
        });

        FullSpeedLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        FullSpeedLabel.setText("100% Speed");

        EightButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        EightButton.setText("8");
        EightButton.setFocusable(false);
        EightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EightButtonActionPerformed(evt);
            }
        });

        NineButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NineButton.setText("9");
        NineButton.setFocusable(false);
        NineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NineButtonActionPerformed(evt);
            }
        });

        SevenButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        SevenButton.setText("7");
        SevenButton.setFocusable(false);
        SevenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SevenButtonActionPerformed(evt);
            }
        });

        FourButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        FourButton.setText("4");
        FourButton.setFocusable(false);
        FourButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FourButtonActionPerformed(evt);
            }
        });

        ThreeButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ThreeButton.setText("3");
        ThreeButton.setFocusable(false);
        ThreeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ThreeButtonActionPerformed(evt);
            }
        });

        TwoButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        TwoButton.setText("2");
        TwoButton.setFocusable(false);
        TwoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TwoButtonActionPerformed(evt);
            }
        });

        OneButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        OneButton.setText("1");
        OneButton.setFocusable(false);
        OneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OneButtonActionPerformed(evt);
            }
        });

        SixButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        SixButton.setText("6");
        SixButton.setFocusable(false);
        SixButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SixButtonActionPerformed(evt);
            }
        });

        FiveButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        FiveButton.setText("5");
        FiveButton.setFocusable(false);
        FiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FiveButtonActionPerformed(evt);
            }
        });

        ZeroPercentSpeedLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        ZeroPercentSpeedLabel.setText("0% Speed");

        javax.swing.GroupLayout controlsPanelLayout = new javax.swing.GroupLayout(controlsPanel);
        controlsPanel.setLayout(controlsPanelLayout);
        controlsPanelLayout.setHorizontalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addComponent(ZeroPercentSpeedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(FullSpeedLabel))
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(controlsPanelLayout.createSequentialGroup()
                                    .addComponent(OneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(6, 6, 6)
                                    .addComponent(TwoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(ShiftButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(SlowStopLabel))
                        .addGap(6, 6, 6)
                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(controlsPanelLayout.createSequentialGroup()
                                    .addComponent(ThreeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(6, 6, 6)
                                    .addComponent(FourButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(SpacebarButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel8))
                        .addGap(6, 6, 6)
                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(controlsPanelLayout.createSequentialGroup()
                                    .addComponent(FiveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(6, 6, 6)
                                    .addComponent(SixButton, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                                .addComponent(AltEmergencyStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(EStopLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(controlsPanelLayout.createSequentialGroup()
                                .addComponent(DirectionLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(controlsPanelLayout.createSequentialGroup()
                                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(controlsPanelLayout.createSequentialGroup()
                                        .addComponent(SevenButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(LeftArrow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(6, 6, 6)
                                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(controlsPanelLayout.createSequentialGroup()
                                        .addComponent(EightButton, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                                        .addGap(6, 6, 6)
                                        .addComponent(NineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(6, 6, 6)
                                        .addComponent(ZeroButton, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                                    .addGroup(controlsPanelLayout.createSequentialGroup()
                                        .addComponent(RightArrow, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(6, 6, 6)
                                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(controlsPanelLayout.createSequentialGroup()
                                                .addComponent(UpArrow, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(6, 6, 6)
                                                .addComponent(DownArrow, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        controlsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {EightButton, FiveButton, FourButton, NineButton, OneButton, SevenButton, SixButton, ThreeButton, TwoButton, ZeroButton});

        controlsPanelLayout.setVerticalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(NineButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ZeroButton))
                    .addComponent(EightButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SevenButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SixButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FiveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FourButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ThreeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TwoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(OneButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ZeroPercentSpeedLabel)
                    .addComponent(FullSpeedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SpacebarButton)
                    .addComponent(ShiftButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AltEmergencyStop)
                    .addComponent(LeftArrow)
                    .addComponent(RightArrow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(UpArrow)
                    .addComponent(DownArrow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(EStopLabel)
                    .addComponent(DirectionLabel)
                    .addComponent(jLabel7)
                    .addComponent(SlowStopLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PrimaryControls.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        PrimaryControls.setForeground(new java.awt.Color(0, 0, 155));
        PrimaryControls.setText("Primary Keyboard Controls");

        latencyLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        latencyLabel.setForeground(new java.awt.Color(255, 0, 0));
        latencyLabel.setText("Latency:");
        latencyLabel.setToolTipText("Network latency should consistently be low to ensure a stable connection.");

        toggleMenuBar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        toggleMenuBar.setSelected(true);
        toggleMenuBar.setText("Toggle Menu Bar");
        toggleMenuBar.setToolTipText("Control+M");
        toggleMenuBar.setFocusable(false);
        toggleMenuBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleMenuBarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LocControlPanelLayout = new javax.swing.GroupLayout(LocControlPanel);
        LocControlPanel.setLayout(LocControlPanelLayout);
        LocControlPanelLayout.setHorizontalGroup(
            LocControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LocControlPanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(LocControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(LocControlPanelLayout.createSequentialGroup()
                        .addComponent(PrimaryControls)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(latencyLabel))
                    .addGroup(LocControlPanelLayout.createSequentialGroup()
                        .addComponent(locMappingLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(toggleMenuBar))
                    .addComponent(controlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(LocContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        LocControlPanelLayout.setVerticalGroup(
            LocControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LocControlPanelLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(LocControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locMappingLabel)
                    .addComponent(toggleMenuBar))
                .addGap(3, 3, 3)
                .addComponent(LocContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(LocControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PrimaryControls)
                    .addComponent(latencyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );

        KeyboardTab.addTab("Ctrl", LocControlPanel);

        layoutPanel.setBackground(new java.awt.Color(238, 238, 238));
        layoutPanel.setFocusable(false);
        layoutPanel.setMaximumSize(new java.awt.Dimension(806, 589));
        layoutPanel.setMinimumSize(new java.awt.Dimension(806, 589));
        layoutPanel.setPreferredSize(new java.awt.Dimension(806, 589));

        LayoutList.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        LayoutList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        LayoutList.setToolTipText("- / + to cycle");
        LayoutList.setFocusable(false);
        LayoutList.setMinimumSize(new java.awt.Dimension(90, 24));
        LayoutList.setPreferredSize(new java.awt.Dimension(90, 24));
        LayoutList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LayoutListActionPerformed(evt);
            }
        });

        layoutListLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        layoutListLabel.setForeground(new java.awt.Color(0, 0, 115));
        layoutListLabel.setText("Layout");
        layoutListLabel.setToolTipText("+ / -");

        LayoutArea.setBackground(new java.awt.Color(255, 255, 255));
        LayoutArea.setMaximumSize(null);
        LayoutArea.setMinimumSize(null);

        InnerLayoutPanel.setBackground(new java.awt.Color(255, 255, 255));
        InnerLayoutPanel.setMaximumSize(null);

        javax.swing.GroupLayout InnerLayoutPanelLayout = new javax.swing.GroupLayout(InnerLayoutPanel);
        InnerLayoutPanel.setLayout(InnerLayoutPanelLayout);
        InnerLayoutPanelLayout.setHorizontalGroup(
            InnerLayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 916, Short.MAX_VALUE)
        );
        InnerLayoutPanelLayout.setVerticalGroup(
            InnerLayoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
        );

        LayoutArea.setViewportView(InnerLayoutPanel);

        sizeLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        sizeLabel.setForeground(new java.awt.Color(0, 0, 115));
        sizeLabel.setText("Size");

        SizeList.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        SizeList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Small", "Large" }));
        SizeList.setFocusable(false);
        SizeList.setMinimumSize(new java.awt.Dimension(90, 22));
        SizeList.setPreferredSize(new java.awt.Dimension(90, 22));
        SizeList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SizeListActionPerformed(evt);
            }
        });

        layoutNewWindow.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        layoutNewWindow.setText("Large");
        layoutNewWindow.setFocusable(false);
        layoutNewWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutNewWindowActionPerformed(evt);
            }
        });

        smallButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        smallButton.setText("Small");
        smallButton.setFocusable(false);
        smallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                smallButtonActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("Pop-up:");
        jLabel19.setToolTipText("Show the current track diagram in a pop-up window");

        allButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        allButton.setText("All");
        allButton.setFocusable(false);
        allButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allButtonActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        editLayoutButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        editLayoutButton.setText("Edit");
        editLayoutButton.setToolTipText("Launch the layout editor");
        editLayoutButton.setFocusable(false);
        editLayoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLayoutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layoutPanelLayout = new javax.swing.GroupLayout(layoutPanel);
        layoutPanel.setLayout(layoutPanelLayout);
        layoutPanelLayout.setHorizontalGroup(
            layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LayoutArea, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layoutPanelLayout.createSequentialGroup()
                        .addComponent(layoutListLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LayoutList, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SizeList, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editLayoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(smallButton, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(layoutNewWindow, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(allButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layoutPanelLayout.setVerticalGroup(
            layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(LayoutArea, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SizeList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(LayoutList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(layoutListLabel)
                        .addComponent(sizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(editLayoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(allButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(layoutNewWindow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(smallButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1))
                .addContainerGap())
        );

        KeyboardTab.addTab("Diag", layoutPanel);

        autoPanel.setBackground(new java.awt.Color(255, 255, 255));
        autoPanel.setMaximumSize(null);
        autoPanel.setPreferredSize(new java.awt.Dimension(806, 589));

        locCommandPanels.setBackground(new java.awt.Color(255, 255, 255));
        locCommandPanels.setFocusable(false);
        locCommandPanels.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        locCommandPanels.setPreferredSize(new java.awt.Dimension(733, 581));
        locCommandPanels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                locCommandPanelsMouseClicked(evt);
            }
        });

        autonomyPanel.setBackground(new java.awt.Color(255, 255, 255));

        autonomyJSON.setColumns(20);
        autonomyJSON.setFont(new java.awt.Font("Monospaced", 0, 16)); // NOI18N
        autonomyJSON.setRows(5);
        jScrollPane2.setViewportView(autonomyJSON);

        jLabel6.setForeground(new java.awt.Color(0, 0, 115));

        exportJSON.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        exportJSON.setText("Export Current Graph");
        exportJSON.setEnabled(false);
        exportJSON.setFocusable(false);
        exportJSON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportJSONActionPerformed(evt);
            }
        });

        loadJSONButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        loadJSONButton.setText("Import Configuration from File");
        loadJSONButton.setFocusable(false);
        loadJSONButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadJSONButtonActionPerformed(evt);
            }
        });

        autosave.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        autosave.setSelected(true);
        autosave.setText("Auto-save on exit");
        autosave.setToolTipText("If unchecked, be sure to manually export the graph prior to exiting.");
        autosave.setFocusable(false);
        autosave.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        autosave.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        autosave.setMaximumSize(new java.awt.Dimension(139, 20));
        autosave.setMinimumSize(new java.awt.Dimension(139, 20));
        autosave.setPreferredSize(new java.awt.Dimension(139, 20));
        autosave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autosaveActionPerformed(evt);
            }
        });

        jsonDocumentationButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jsonDocumentationButton.setForeground(new java.awt.Color(0, 0, 155));
        jsonDocumentationButton.setText("Documentation");
        jsonDocumentationButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jsonDocumentationButton.setBorderPainted(false);
        jsonDocumentationButton.setContentAreaFilled(false);
        jsonDocumentationButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jsonDocumentationButton.setFocusable(false);
        jsonDocumentationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jsonDocumentationButtonActionPerformed(evt);
            }
        });

        loadDefaultBlankGraph.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        loadDefaultBlankGraph.setForeground(new java.awt.Color(0, 0, 155));
        loadDefaultBlankGraph.setText("Initialize New Configuration");
        loadDefaultBlankGraph.setToolTipText("Creates a blank graph that you can edit visually.");
        loadDefaultBlankGraph.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        loadDefaultBlankGraph.setContentAreaFilled(false);
        loadDefaultBlankGraph.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        loadDefaultBlankGraph.setFocusable(false);
        loadDefaultBlankGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDefaultBlankGraphActionPerformed(evt);
            }
        });

        validateButton.setBackground(new java.awt.Color(204, 255, 204));
        validateButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        validateButton.setText("Validate Configuration & Open Graph UI");
        validateButton.setToolTipText("Parses the JSON configuration data and displays the graph UI.  Force stops any running trains.");
        validateButton.setFocusable(false);
        validateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateButtonActionPerformed(evt);
            }
        });

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout autonomyPanelLayout = new javax.swing.GroupLayout(autonomyPanel);
        autonomyPanel.setLayout(autonomyPanelLayout);
        autonomyPanelLayout.setHorizontalGroup(
            autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autonomyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, autonomyPanelLayout.createSequentialGroup()
                        .addGroup(autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(autonomyPanelLayout.createSequentialGroup()
                                .addComponent(validateButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(loadDefaultBlankGraph)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jsonDocumentationButton))
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addGroup(autonomyPanelLayout.createSequentialGroup()
                        .addComponent(loadJSONButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportJSON, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 221, Short.MAX_VALUE)
                        .addComponent(autosave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        autonomyPanelLayout.setVerticalGroup(
            autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autonomyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jsonDocumentationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadDefaultBlankGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(validateButton)
                    .addComponent(jSeparator4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(autonomyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(exportJSON, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(autosave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(loadJSONButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(autonomyPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel6)
                .addContainerGap(519, Short.MAX_VALUE))
        );

        locCommandPanels.addTab("Autonomy Configuration", autonomyPanel);

        locCommandTab.setBackground(new java.awt.Color(255, 255, 255));
        locCommandTab.setMaximumSize(new java.awt.Dimension(718, 5000));

        jScrollPane4.setBackground(new java.awt.Color(238, 238, 238));
        jScrollPane4.setPreferredSize(new java.awt.Dimension(718, 421));

        autoLocPanel.setBackground(new java.awt.Color(255, 255, 255));
        autoLocPanel.setEnabled(false);
        autoLocPanel.setFocusable(false);
        autoLocPanel.setMaximumSize(new java.awt.Dimension(716, 5000));
        autoLocPanel.setLayout(new java.awt.GridLayout(100, 3, 5, 5));
        jScrollPane4.setViewportView(autoLocPanel);

        gracefulStop.setBackground(new java.awt.Color(255, 204, 204));
        gracefulStop.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        gracefulStop.setText("Graceful Stop");
        gracefulStop.setToolTipText("Active locomotives will stop at the next station.");
        gracefulStop.setEnabled(false);
        gracefulStop.setFocusable(false);
        gracefulStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gracefulStopActionPerformed(evt);
            }
        });

        startAutonomy.setBackground(new java.awt.Color(204, 255, 204));
        startAutonomy.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        startAutonomy.setText("Start Autonomous Operation");
        startAutonomy.setToolTipText("Continuously runs active locomotives within the graph.");
        startAutonomy.setEnabled(false);
        startAutonomy.setFocusable(false);
        startAutonomy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startAutonomyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout locCommandTabLayout = new javax.swing.GroupLayout(locCommandTab);
        locCommandTab.setLayout(locCommandTabLayout);
        locCommandTabLayout.setHorizontalGroup(
            locCommandTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(locCommandTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(locCommandTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 733, Short.MAX_VALUE)
                    .addGroup(locCommandTabLayout.createSequentialGroup()
                        .addComponent(gracefulStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(startAutonomy)))
                .addContainerGap())
        );
        locCommandTabLayout.setVerticalGroup(
            locCommandTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, locCommandTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(locCommandTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startAutonomy)
                    .addComponent(gracefulStop))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                .addContainerGap())
        );

        locCommandPanels.addTab("Locomotive Commands", locCommandTab);

        timetablePanel.setBackground(new java.awt.Color(255, 255, 255));

        timetable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        timetable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        timetable.setGridColor(new java.awt.Color(242, 242, 242));
        jScrollPane6.setViewportView(timetable);

        executeTimetable.setBackground(new java.awt.Color(204, 255, 204));
        executeTimetable.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        executeTimetable.setText("Execute Timetable");
        executeTimetable.setToolTipText("Sequentially executes the timetable.");
        executeTimetable.setFocusable(false);
        executeTimetable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeTimetableActionPerformed(evt);
            }
        });

        timetableCapture.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        timetableCapture.setText("Capture Locomotive Commands");
        timetableCapture.setToolTipText("Press this, then start autonomous operation or run locomotive commands to add them to the timetable.  Try to end where you started!");
        timetableCapture.setFocusable(false);
        timetableCapture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timetableCaptureActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout timetablePanelLayout = new javax.swing.GroupLayout(timetablePanel);
        timetablePanel.setLayout(timetablePanelLayout);
        timetablePanelLayout.setHorizontalGroup(
            timetablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timetablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(timetablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 733, Short.MAX_VALUE)
                    .addGroup(timetablePanelLayout.createSequentialGroup()
                        .addComponent(timetableCapture)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(executeTimetable)))
                .addContainerGap())
        );
        timetablePanelLayout.setVerticalGroup(
            timetablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, timetablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(timetablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(executeTimetable)
                    .addComponent(timetableCapture))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                .addContainerGap())
        );

        locCommandPanels.addTab("Timetable", timetablePanel);

        autoSettingsPanel.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(245, 245, 245));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel46.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(0, 0, 115));
        jLabel46.setText("Minimum Action Delay (s)");
        jLabel46.setFocusable(false);

        minDelay.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        minDelay.setMajorTickSpacing(10);
        minDelay.setMaximum(30);
        minDelay.setMinorTickSpacing(1);
        minDelay.setPaintLabels(true);
        minDelay.setPaintTicks(true);
        minDelay.setToolTipText("Minimum number of seconds to sleep before a locomotive moves.");
        minDelay.setFocusable(false);
        minDelay.setMaximumSize(new java.awt.Dimension(230, 55));
        minDelay.setPreferredSize(new java.awt.Dimension(230, 55));
        minDelay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minDelayStateChanged(evt);
            }
        });
        minDelay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minDelayMouseReleased(evt);
            }
        });

        jLabel48.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(0, 0, 115));
        jLabel48.setText("Prioritize Locomotives After (min)");
        jLabel48.setFocusable(false);

        maxLocInactiveSeconds.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        maxLocInactiveSeconds.setMajorTickSpacing(5);
        maxLocInactiveSeconds.setMaximum(10);
        maxLocInactiveSeconds.setMinorTickSpacing(1);
        maxLocInactiveSeconds.setPaintLabels(true);
        maxLocInactiveSeconds.setPaintTicks(true);
        maxLocInactiveSeconds.setToolTipText("When >0, locomotives idle for longer than this will be prioritized.");
        maxLocInactiveSeconds.setFocusable(false);
        maxLocInactiveSeconds.setMaximumSize(new java.awt.Dimension(230, 55));
        maxLocInactiveSeconds.setPreferredSize(new java.awt.Dimension(230, 55));
        maxLocInactiveSeconds.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxLocInactiveSecondsMouseReleased(evt);
            }
        });

        jLabel47.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(0, 0, 115));
        jLabel47.setText("Maximum Action Delay (s)");
        jLabel47.setFocusable(false);

        maxDelay.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        maxDelay.setMajorTickSpacing(10);
        maxDelay.setMaximum(30);
        maxDelay.setMinorTickSpacing(1);
        maxDelay.setPaintLabels(true);
        maxDelay.setPaintTicks(true);
        maxDelay.setToolTipText("Maximum number of seconds to sleep before a locomotive moves.");
        maxDelay.setFocusable(false);
        maxDelay.setMaximumSize(new java.awt.Dimension(230, 55));
        maxDelay.setPreferredSize(new java.awt.Dimension(230, 55));
        maxDelay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxDelayMouseReleased(evt);
            }
        });

        jLabel43.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(0, 0, 115));
        jLabel43.setText("Default Locomotive Speed (%)");
        jLabel43.setFocusable(false);

        defaultLocSpeed.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        defaultLocSpeed.setMajorTickSpacing(20);
        defaultLocSpeed.setMinorTickSpacing(5);
        defaultLocSpeed.setPaintLabels(true);
        defaultLocSpeed.setPaintTicks(true);
        defaultLocSpeed.setToolTipText("The speed at which locomotives will run at by defualt.");
        defaultLocSpeed.setFocusable(false);
        defaultLocSpeed.setMaximumSize(new java.awt.Dimension(230, 55));
        defaultLocSpeed.setPreferredSize(new java.awt.Dimension(230, 55));
        defaultLocSpeed.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                defaultLocSpeedMouseReleased(evt);
            }
        });

        jLabel49.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(0, 0, 115));
        jLabel49.setText("Pre-arrival Speed Multiplier (%)");
        jLabel49.setFocusable(false);

        preArrivalSpeedReduction.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        preArrivalSpeedReduction.setMajorTickSpacing(10);
        preArrivalSpeedReduction.setMinimum(10);
        preArrivalSpeedReduction.setMinorTickSpacing(5);
        preArrivalSpeedReduction.setPaintLabels(true);
        preArrivalSpeedReduction.setPaintTicks(true);
        preArrivalSpeedReduction.setToolTipText("Locomotives slow down when they are about to reach their station.  This controls by how much to slow them down.");
        preArrivalSpeedReduction.setFocusable(false);
        preArrivalSpeedReduction.setMaximumSize(new java.awt.Dimension(230, 55));
        preArrivalSpeedReduction.setPreferredSize(new java.awt.Dimension(230, 55));
        preArrivalSpeedReduction.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                preArrivalSpeedReductionMouseReleased(evt);
            }
        });

        jLabel50.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(0, 0, 115));
        jLabel50.setText("Other Settings");
        jLabel50.setFocusable(false);

        atomicRoutes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        atomicRoutes.setText("Atomic Routes");
        atomicRoutes.setToolTipText("When unchecked, edges will unlock as trains pass them, for a more dynamic experience.  Edge and train lengths need to be set for best results.");
        atomicRoutes.setFocusable(false);
        atomicRoutes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                atomicRoutesMouseReleased(evt);
            }
        });

        turnOffFunctionsOnArrival.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        turnOffFunctionsOnArrival.setText("Turn Off Functions on Arrival");
        turnOffFunctionsOnArrival.setToolTipText("Controls whether preset functions are turned off when a locomotive reaches its station.");
        turnOffFunctionsOnArrival.setFocusable(false);
        turnOffFunctionsOnArrival.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                turnOffFunctionsOnArrivalMouseReleased(evt);
            }
        });

        simulate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        simulate.setText("Simulate");
        simulate.setToolTipText("Enable simulation of routes in debug mode.");
        simulate.setFocusable(false);
        simulate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                simulateMouseReleased(evt);
            }
        });

        turnOnFunctionsOnDeparture.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        turnOnFunctionsOnDeparture.setText("Turn On Functions on Departure");
        turnOnFunctionsOnDeparture.setToolTipText("Controls whether preset functions are turned on when a locomotive departs its station.");
        turnOnFunctionsOnDeparture.setFocusable(false);
        turnOnFunctionsOnDeparture.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                turnOnFunctionsOnDepartureMouseReleased(evt);
            }
        });

        maximumLatency.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        maximumLatency.setMajorTickSpacing(1000);
        maximumLatency.setMaximum(4000);
        maximumLatency.setMinorTickSpacing(250);
        maximumLatency.setPaintLabels(true);
        maximumLatency.setPaintTicks(true);
        maximumLatency.setToolTipText("If greater than 0, the power will automatically be turned off if the network latency exceeds this value.");
        maximumLatency.setValue(0);
        maximumLatency.setFocusable(false);
        maximumLatency.setMaximumSize(new java.awt.Dimension(230, 55));
        maximumLatency.setPreferredSize(new java.awt.Dimension(230, 55));
        maximumLatency.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maximumLatencyMouseReleased(evt);
            }
        });

        jLabel44.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(0, 0, 115));
        jLabel44.setText("Maximum Network Latency (ms)");
        jLabel44.setFocusable(false);

        maxActiveTrains.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        maxActiveTrains.setMajorTickSpacing(5);
        maxActiveTrains.setMaximum(10);
        maxActiveTrains.setMinorTickSpacing(1);
        maxActiveTrains.setPaintLabels(true);
        maxActiveTrains.setPaintTicks(true);
        maxActiveTrains.setToolTipText("Controls the maximum number of concurrent trains (0 to disable).");
        maxActiveTrains.setFocusable(false);
        maxActiveTrains.setMaximumSize(new java.awt.Dimension(230, 55));
        maxActiveTrains.setPreferredSize(new java.awt.Dimension(230, 55));
        maxActiveTrains.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxActiveTrainsMouseReleased(evt);
            }
        });

        jLabel53.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(0, 0, 115));
        jLabel53.setText("Maximum Active Trains");
        jLabel53.setToolTipText("");
        jLabel53.setFocusable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(simulate)
                    .addComponent(jLabel46)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(maxDelay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(minDelay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel47))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel43)
                            .addComponent(maxLocInactiveSeconds, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel48)
                            .addComponent(defaultLocSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel49)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(turnOnFunctionsOnDeparture, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(preArrivalSpeedReduction, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(atomicRoutes)
                            .addComponent(turnOffFunctionsOnArrival)
                            .addComponent(jLabel50))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxActiveTrains, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel53)
                            .addComponent(jLabel44)
                            .addComponent(maximumLatency, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel46)
                            .addComponent(jLabel48))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minDelay, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxLocInactiveSeconds, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel47)
                            .addComponent(jLabel43))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(maxDelay, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(defaultLocSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel49)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(preArrivalSpeedReduction, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel44)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maximumLatency, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel50)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(atomicRoutes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(turnOffFunctionsOnArrival))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel53)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxActiveTrains, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(turnOnFunctionsOnDeparture)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(simulate))
        );

        jLabel51.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(0, 0, 115));
        jLabel51.setText("Train Behavior");

        jPanel4.setBackground(new java.awt.Color(245, 245, 245));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        hideReversing.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hideReversing.setText("Hide Reversing Stations");
        hideReversing.setToolTipText("Temporarily hides reversing stations from view in the graph.");
        hideReversing.setFocusable(false);
        hideReversing.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                hideReversingMouseReleased(evt);
            }
        });

        hideInactive.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        hideInactive.setText("Hide Inactive Points");
        hideInactive.setToolTipText("Temporarily hides manually deactivated points from view in the graph.");
        hideInactive.setFocusable(false);
        hideInactive.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                hideInactiveMouseReleased(evt);
            }
        });

        showStationLengths.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        showStationLengths.setText("Show Lengths & Exclusions");
        showStationLengths.setToolTipText("Displays edge lengths, maximum train lengths next to each station name, and highlights locomotive exclusions.");
        showStationLengths.setFocusable(false);
        showStationLengths.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                showStationLengthsMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hideReversing)
                    .addComponent(hideInactive)
                    .addComponent(showStationLengths))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(hideReversing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hideInactive)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showStationLengths, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel52.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(0, 0, 115));
        jLabel52.setText("Graph UI Options");

        javax.swing.GroupLayout autoSettingsPanelLayout = new javax.swing.GroupLayout(autoSettingsPanel);
        autoSettingsPanel.setLayout(autoSettingsPanelLayout);
        autoSettingsPanelLayout.setHorizontalGroup(
            autoSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autoSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autoSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 493, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel51))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(autoSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel52))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        autoSettingsPanelLayout.setVerticalGroup(
            autoSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autoSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autoSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel52))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(autoSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        locCommandPanels.addTab("Autonomy Settings", autoSettingsPanel);

        javax.swing.GroupLayout autoPanelLayout = new javax.swing.GroupLayout(autoPanel);
        autoPanel.setLayout(autoPanelLayout);
        autoPanelLayout.setHorizontalGroup(
            autoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autoPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(locCommandPanels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        autoPanelLayout.setVerticalGroup(
            autoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(locCommandPanels, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );

        KeyboardTab.addTab("Auto", autoPanel);

        KeyboardPanel.setBackground(new java.awt.Color(255, 255, 255));
        KeyboardPanel.setToolTipText(null);
        KeyboardPanel.setFocusable(false);
        KeyboardPanel.setMaximumSize(new java.awt.Dimension(806, 589));
        KeyboardPanel.setMinimumSize(new java.awt.Dimension(806, 589));
        KeyboardPanel.setPreferredSize(new java.awt.Dimension(806, 589));

        KeyboardLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        KeyboardLabel.setForeground(new java.awt.Color(0, 0, 115));
        KeyboardLabel.setText("Accessory Addresses");
        KeyboardLabel.setFocusable(false);

        keyboardButtonPanel.setBackground(new java.awt.Color(245, 245, 245));
        keyboardButtonPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        keyboardButtonPanel.setFocusable(false);

        SwitchButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton1.setText("1");
        SwitchButton1.setFocusable(false);
        SwitchButton1.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton1.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton1.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton2.setText("2");
        SwitchButton2.setFocusable(false);
        SwitchButton2.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton2.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton2.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton3.setText("3");
        SwitchButton3.setFocusable(false);
        SwitchButton3.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton3.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton3.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton4.setText("4");
        SwitchButton4.setFocusable(false);
        SwitchButton4.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton4.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton4.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton5.setText("5");
        SwitchButton5.setFocusable(false);
        SwitchButton5.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton5.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton5.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton6.setText("6");
        SwitchButton6.setFocusable(false);
        SwitchButton6.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton6.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton6.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton7.setText("7");
        SwitchButton7.setFocusable(false);
        SwitchButton7.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton7.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton7.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton8.setText("8");
        SwitchButton8.setFocusable(false);
        SwitchButton8.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton8.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton8.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton9.setText("9");
        SwitchButton9.setFocusable(false);
        SwitchButton9.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton9.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton9.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton10.setText("10");
        SwitchButton10.setFocusable(false);
        SwitchButton10.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton10.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton10.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton11.setText("11");
        SwitchButton11.setFocusable(false);
        SwitchButton11.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton11.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton11.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton12.setText("12");
        SwitchButton12.setFocusable(false);
        SwitchButton12.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton12.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton12.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton13.setText("13");
        SwitchButton13.setFocusable(false);
        SwitchButton13.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton13.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton13.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton14.setText("14");
        SwitchButton14.setFocusable(false);
        SwitchButton14.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton14.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton14.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton15.setText("15");
        SwitchButton15.setFocusable(false);
        SwitchButton15.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton15.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton15.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton16.setText("16");
        SwitchButton16.setFocusable(false);
        SwitchButton16.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton16.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton16.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton17.setText("17");
        SwitchButton17.setFocusable(false);
        SwitchButton17.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton17.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton17.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton18.setText("18");
        SwitchButton18.setFocusable(false);
        SwitchButton18.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton18.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton18.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton19.setText("19");
        SwitchButton19.setFocusable(false);
        SwitchButton19.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton19.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton19.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton21.setText("21");
        SwitchButton21.setFocusable(false);
        SwitchButton21.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton21.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton21.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton22.setText("22");
        SwitchButton22.setFocusable(false);
        SwitchButton22.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton22.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton22.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton23.setText("23");
        SwitchButton23.setFocusable(false);
        SwitchButton23.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton23.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton23.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton24.setText("24");
        SwitchButton24.setFocusable(false);
        SwitchButton24.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton24.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton24.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton26.setText("26");
        SwitchButton26.setFocusable(false);
        SwitchButton26.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton26.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton26.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton27.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton27.setText("27");
        SwitchButton27.setFocusable(false);
        SwitchButton27.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton27.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton27.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton29.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton29.setText("29");
        SwitchButton29.setFocusable(false);
        SwitchButton29.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton29.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton29.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton28.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton28.setText("28");
        SwitchButton28.setFocusable(false);
        SwitchButton28.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton28.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton28.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton30.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton30.setText("30");
        SwitchButton30.setFocusable(false);
        SwitchButton30.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton30.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton30.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton31.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton31.setText("31");
        SwitchButton31.setFocusable(false);
        SwitchButton31.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton31.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton31.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton32.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton32.setText("32");
        SwitchButton32.setFocusable(false);
        SwitchButton32.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton32.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton32.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton33.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton33.setText("33");
        SwitchButton33.setFocusable(false);
        SwitchButton33.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton33.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton33.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton34.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton34.setText("34");
        SwitchButton34.setFocusable(false);
        SwitchButton34.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton34.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton34.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton35.setText("35");
        SwitchButton35.setFocusable(false);
        SwitchButton35.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton35.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton35.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton36.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton36.setText("36");
        SwitchButton36.setFocusable(false);
        SwitchButton36.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton36.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton36.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton37.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton37.setText("37");
        SwitchButton37.setFocusable(false);
        SwitchButton37.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton37.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton37.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton38.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton38.setText("38");
        SwitchButton38.setFocusable(false);
        SwitchButton38.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton38.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton38.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton39.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton39.setText("39");
        SwitchButton39.setFocusable(false);
        SwitchButton39.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton39.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton39.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton40.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton40.setText("40");
        SwitchButton40.setFocusable(false);
        SwitchButton40.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton40.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton40.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton41.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton41.setText("41");
        SwitchButton41.setFocusable(false);
        SwitchButton41.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton41.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton41.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton42.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton42.setText("42");
        SwitchButton42.setFocusable(false);
        SwitchButton42.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton42.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton42.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton43.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton43.setText("43");
        SwitchButton43.setFocusable(false);
        SwitchButton43.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton43.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton43.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton43.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton44.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton44.setText("44");
        SwitchButton44.setFocusable(false);
        SwitchButton44.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton44.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton44.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton44.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton45.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton45.setText("45");
        SwitchButton45.setFocusable(false);
        SwitchButton45.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton45.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton45.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton45.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton46.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton46.setText("46");
        SwitchButton46.setFocusable(false);
        SwitchButton46.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton46.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton46.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton46.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton47.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton47.setText("47");
        SwitchButton47.setFocusable(false);
        SwitchButton47.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton47.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton47.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton47.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton48.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton48.setText("48");
        SwitchButton48.setFocusable(false);
        SwitchButton48.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton48.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton48.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton48.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton49.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton49.setText("49");
        SwitchButton49.setFocusable(false);
        SwitchButton49.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton49.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton49.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton49.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton50.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton50.setText("50");
        SwitchButton50.setFocusable(false);
        SwitchButton50.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton50.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton50.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton51.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton51.setText("51");
        SwitchButton51.setFocusable(false);
        SwitchButton51.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton51.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton51.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton51.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton52.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton52.setText("52");
        SwitchButton52.setFocusable(false);
        SwitchButton52.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton52.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton52.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton52.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton53.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton53.setText("53");
        SwitchButton53.setFocusable(false);
        SwitchButton53.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton53.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton53.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton53.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton54.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton54.setText("54");
        SwitchButton54.setFocusable(false);
        SwitchButton54.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton54.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton54.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton54.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton55.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton55.setText("55");
        SwitchButton55.setFocusable(false);
        SwitchButton55.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton55.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton55.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton55.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton57.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton57.setText("57");
        SwitchButton57.setFocusable(false);
        SwitchButton57.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton57.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton57.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton57.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton58.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton58.setText("58");
        SwitchButton58.setFocusable(false);
        SwitchButton58.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton58.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton58.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton58.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton59.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton59.setText("59");
        SwitchButton59.setFocusable(false);
        SwitchButton59.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton59.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton59.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton59.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton60.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton60.setText("60");
        SwitchButton60.setFocusable(false);
        SwitchButton60.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton60.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton60.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton60.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton61.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton61.setText("61");
        SwitchButton61.setFocusable(false);
        SwitchButton61.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton61.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton61.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton61.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton62.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton62.setText("62");
        SwitchButton62.setFocusable(false);
        SwitchButton62.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton62.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton62.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton62.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton63.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton63.setText("63");
        SwitchButton63.setFocusable(false);
        SwitchButton63.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton63.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton63.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton63.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton20.setText("20");
        SwitchButton20.setFocusable(false);
        SwitchButton20.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton20.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton20.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton56.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton56.setText("56");
        SwitchButton56.setFocusable(false);
        SwitchButton56.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton56.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton56.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton56.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton25.setText("25");
        SwitchButton25.setFocusable(false);
        SwitchButton25.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton25.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton25.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        SwitchButton64.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SwitchButton64.setText("64");
        SwitchButton64.setFocusable(false);
        SwitchButton64.setMaximumSize(new java.awt.Dimension(24, 20));
        SwitchButton64.setMinimumSize(new java.awt.Dimension(24, 20));
        SwitchButton64.setPreferredSize(new java.awt.Dimension(24, 20));
        SwitchButton64.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateSwitchState(evt);
            }
        });

        jSeparator9.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator9.setPreferredSize(new java.awt.Dimension(50, 9));

        jSeparator10.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator11.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator12.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator13.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator14.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator15.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout keyboardButtonPanelLayout = new javax.swing.GroupLayout(keyboardButtonPanel);
        keyboardButtonPanel.setLayout(keyboardButtonPanelLayout);
        keyboardButtonPanelLayout.setHorizontalGroup(
            keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyboardButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton41, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton49, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton57, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton42, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton58, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton43, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton59, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator11, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton52, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton60, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton45, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton53, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton61, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator14, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton30, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton46, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton54, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton62, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator13, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton31, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton47, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton55, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton63, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator15, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SwitchButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton48, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton56, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton64, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SwitchButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        keyboardButtonPanelLayout.setVerticalGroup(
            keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyboardButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator11)
                    .addComponent(jSeparator10)
                    .addComponent(jSeparator14)
                    .addComponent(jSeparator15)
                    .addGroup(keyboardButtonPanelLayout.createSequentialGroup()
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SwitchButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(SwitchButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(SwitchButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(SwitchButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(SwitchButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(SwitchButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(SwitchButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(SwitchButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton30, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton31, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton32, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton33, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton34, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton40, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton41, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton42, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton43, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton44, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton45, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton46, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton47, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton48, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton49, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton50, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton51, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton52, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton53, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton54, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton55, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton56, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(keyboardButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SwitchButton57, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton58, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton59, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton60, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton61, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton62, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton63, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SwitchButton64, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jSeparator9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator12)
                    .addComponent(jSeparator13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBackground(new java.awt.Color(245, 245, 245));
        jPanel10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel10.setFocusable(false);

        PrevKeyboard.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        PrevKeyboard.setText("<<< -");
        PrevKeyboard.setFocusable(false);
        PrevKeyboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevKeyboardActionPerformed(evt);
            }
        });

        KeyboardNumberLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        KeyboardNumberLabel.setText("Keyboard ");
        KeyboardNumberLabel.setFocusable(false);

        NextKeyboard.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NextKeyboard.setText("+ >>>");
        NextKeyboard.setFocusable(false);
        NextKeyboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextKeyboardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(KeyboardNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(PrevKeyboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NextKeyboard)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PrevKeyboard)
                    .addComponent(KeyboardNumberLabel)
                    .addComponent(NextKeyboard))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        KeyboardLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        KeyboardLabel1.setForeground(new java.awt.Color(0, 0, 115));
        KeyboardLabel1.setText("Change Page");
        KeyboardLabel1.setFocusable(false);

        buttonGroup5.add(MM2);
        MM2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        MM2.setSelected(true);
        MM2.setText("MM2");
        MM2.setFocusable(false);
        MM2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MM2ActionPerformed(evt);
            }
        });

        buttonGroup5.add(DCC);
        DCC.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        DCC.setText("DCC");
        DCC.setFocusable(false);
        DCC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DCCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout KeyboardPanelLayout = new javax.swing.GroupLayout(KeyboardPanel);
        KeyboardPanel.setLayout(KeyboardPanelLayout);
        KeyboardPanelLayout.setHorizontalGroup(
            KeyboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(KeyboardPanelLayout.createSequentialGroup()
                .addGroup(KeyboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(KeyboardPanelLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(KeyboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(KeyboardLabel)
                            .addComponent(keyboardButtonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(KeyboardLabel1)
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(KeyboardPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(MM2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DCC)))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        KeyboardPanelLayout.setVerticalGroup(
            KeyboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(KeyboardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(KeyboardLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyboardButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(KeyboardLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(KeyboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MM2)
                    .addComponent(DCC))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        KeyboardTab.addTab("Keyb", KeyboardPanel);

        RoutePanel.setBackground(new java.awt.Color(238, 238, 238));
        RoutePanel.setFocusable(false);
        RoutePanel.setMaximumSize(new java.awt.Dimension(806, 589));
        RoutePanel.setMinimumSize(new java.awt.Dimension(806, 589));
        RoutePanel.setPreferredSize(new java.awt.Dimension(806, 589));

        jLabel2.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 115));
        jLabel2.setText("Routes (Click to Execute / Right-click to Edit)");

        RouteList.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        RouteList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        RouteList.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        RouteList.setFocusable(false);
        RouteList.setGridColor(new java.awt.Color(0, 0, 0));
        RouteList.setRowHeight(30);
        RouteList.setRowSelectionAllowed(false);
        RouteList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        RouteList.setTableHeader(null);
        RouteList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                RouteListMouseMoved(evt);
            }
        });
        RouteList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RouteListMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                RouteListMouseExited(evt);
            }
        });
        jScrollPane5.setViewportView(RouteList);

        AddRouteButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        AddRouteButton.setText("Add Route");
        AddRouteButton.setFocusable(false);
        AddRouteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddRouteButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(sortByName);
        sortByName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        sortByName.setText("Name");
        sortByName.setFocusable(false);
        sortByName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByNameActionPerformed(evt);
            }
        });

        buttonGroup2.add(sortByID);
        sortByID.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        sortByID.setText("Route ID");
        sortByID.setFocusable(false);
        sortByID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByIDActionPerformed(evt);
            }
        });

        BulkEnable.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        BulkEnable.setText("Bulk Enable");
        BulkEnable.setFocusable(false);
        BulkEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BulkEnableActionPerformed(evt);
            }
        });

        BulkDisable.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        BulkDisable.setText("Bulk Disable");
        BulkDisable.setFocusable(false);
        BulkDisable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BulkDisableActionPerformed(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setMinimumSize(new java.awt.Dimension(10, 10));
        jSeparator2.setPreferredSize(new java.awt.Dimension(30, 10));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 155));
        jLabel1.setText("Sort by:");

        javax.swing.GroupLayout RoutePanelLayout = new javax.swing.GroupLayout(RoutePanel);
        RoutePanel.setLayout(RoutePanelLayout);
        RoutePanelLayout.setHorizontalGroup(
            RoutePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RoutePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RoutePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5)
                    .addGroup(RoutePanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(RoutePanelLayout.createSequentialGroup()
                        .addComponent(AddRouteButton)
                        .addGap(9, 9, 9)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BulkEnable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BulkDisable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 192, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByID)))
                .addContainerGap())
        );
        RoutePanelLayout.setVerticalGroup(
            RoutePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RoutePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RoutePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AddRouteButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RoutePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sortByName, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(BulkEnable)
                        .addComponent(BulkDisable)
                        .addComponent(sortByID, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
        );

        KeyboardTab.addTab("Rout", RoutePanel);

        logPanel.setBackground(new java.awt.Color(238, 238, 238));
        logPanel.setMaximumSize(new java.awt.Dimension(806, 589));
        logPanel.setMinimumSize(new java.awt.Dimension(806, 589));
        logPanel.setPreferredSize(new java.awt.Dimension(806, 589));

        debugArea.setColumns(20);
        debugArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        debugArea.setRows(5);
        jScrollPane3.setViewportView(debugArea);

        javax.swing.GroupLayout logPanelLayout = new javax.swing.GroupLayout(logPanel);
        logPanel.setLayout(logPanelLayout);
        logPanelLayout.setHorizontalGroup(
            logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 733, Short.MAX_VALUE)
                .addContainerGap())
        );
        logPanelLayout.setVerticalGroup(
            logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );

        KeyboardTab.addTab("Log", logPanel);

        LocFunctionsPanel.setBackground(new java.awt.Color(255, 255, 255));
        LocFunctionsPanel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(192,192,192)));
        LocFunctionsPanel.setToolTipText(null);
        LocFunctionsPanel.setMaximumSize(new java.awt.Dimension(308, 589));
        LocFunctionsPanel.setMinimumSize(new java.awt.Dimension(308, 589));
        LocFunctionsPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                LocFunctionsPanelMouseEntered(evt);
            }
        });
        LocFunctionsPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LocControlPanelKeyPressed(evt);
            }
        });

        OnButton.setBackground(new java.awt.Color(204, 255, 204));
        OnButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        OnButton.setText("ON");
        OnButton.setToolTipText("Alt-G");
        OnButton.setFocusable(false);
        OnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OnButtonActionPerformed(evt);
            }
        });

        PowerOff.setBackground(new java.awt.Color(255, 204, 204));
        PowerOff.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        PowerOff.setText("Power OFF");
        PowerOff.setToolTipText("Escape");
        PowerOff.setFocusable(false);
        PowerOff.setMaximumSize(new java.awt.Dimension(200, 22));
        PowerOff.setMinimumSize(new java.awt.Dimension(200, 22));
        PowerOff.setPreferredSize(new java.awt.Dimension(200, 22));
        PowerOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PowerOffActionPerformed(evt);
            }
        });

        ActiveLocLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 20)); // NOI18N
        ActiveLocLabel.setText("Locomotive Name");
        ActiveLocLabel.setToolTipText("Click to change mapping");
        ActiveLocLabel.setFocusable(false);
        ActiveLocLabel.setMaximumSize(new java.awt.Dimension(296, 25));
        ActiveLocLabel.setMinimumSize(new java.awt.Dimension(296, 25));
        ActiveLocLabel.setPreferredSize(new java.awt.Dimension(296, 25));
        ActiveLocLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ActiveLocLabelMouseReleased(evt);
            }
        });

        SpeedSlider.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        SpeedSlider.setMajorTickSpacing(10);
        SpeedSlider.setMinorTickSpacing(5);
        SpeedSlider.setPaintLabels(true);
        SpeedSlider.setPaintTicks(true);
        SpeedSlider.setToolTipText(null);
        SpeedSlider.setValue(0);
        SpeedSlider.setFocusable(false);
        SpeedSlider.setMinimumSize(new java.awt.Dimension(296, 53));
        SpeedSlider.setPreferredSize(new java.awt.Dimension(296, 53));
        SpeedSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MainSpeedSliderClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                SpeedSliderDragged(evt);
            }
        });

        Backward.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        Backward.setText("<<<<<<<");
        Backward.setToolTipText("Reverse");
        Backward.setFocusable(false);
        Backward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackwardActionPerformed(evt);
            }
        });

        Forward.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        Forward.setText(">>>>>>>");
        Forward.setToolTipText("Forward");
        Forward.setFocusable(false);
        Forward.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ForwardActionPerformed(evt);
            }
        });

        CurrentKeyLabel.setBackground(new java.awt.Color(255, 255, 255));
        CurrentKeyLabel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        CurrentKeyLabel.setForeground(new java.awt.Color(0, 0, 115));
        CurrentKeyLabel.setText("Key Name");
        CurrentKeyLabel.setToolTipText(null);
        CurrentKeyLabel.setFocusable(false);
        CurrentKeyLabel.setMaximumSize(new java.awt.Dimension(296, 18));
        CurrentKeyLabel.setMinimumSize(new java.awt.Dimension(296, 18));
        CurrentKeyLabel.setPreferredSize(new java.awt.Dimension(296, 18));

        locIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        locIcon.setToolTipText("Right-click to change icon");
        locIcon.setFocusable(false);
        locIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        locIcon.setMaximumSize(new java.awt.Dimension(296, 116));
        locIcon.setMinimumSize(new java.awt.Dimension(296, 116));
        locIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                locIconMouseReleased(evt);
            }
        });

        FunctionTabs.setBackground(new java.awt.Color(255, 255, 255));
        FunctionTabs.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        FunctionTabs.setFocusable(false);
        FunctionTabs.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        FunctionTabs.setMinimumSize(new java.awt.Dimension(290, 78));
        FunctionTabs.setPreferredSize(new java.awt.Dimension(293, 173));

        functionPanel.setBackground(new java.awt.Color(255, 255, 255));
        functionPanel.setPreferredSize(new java.awt.Dimension(313, 123));

        F8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F8.setToolTipText("Alt-8 / F8");
        F8.setFocusable(false);
        F8.setMaximumSize(new java.awt.Dimension(75, 33));
        F8.setMinimumSize(new java.awt.Dimension(75, 33));
        F8.setPreferredSize(new java.awt.Dimension(75, 33));
        F8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F9.setToolTipText("Alt-9 / F9");
        F9.setFocusable(false);
        F9.setMaximumSize(new java.awt.Dimension(75, 33));
        F9.setMinimumSize(new java.awt.Dimension(75, 33));
        F9.setPreferredSize(new java.awt.Dimension(75, 33));
        F9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F7.setToolTipText("Alt-7 / F7");
        F7.setFocusable(false);
        F7.setMaximumSize(new java.awt.Dimension(75, 33));
        F7.setMinimumSize(new java.awt.Dimension(75, 33));
        F7.setPreferredSize(new java.awt.Dimension(75, 33));
        F7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F10.setToolTipText("Control-0");
        F10.setFocusable(false);
        F10.setMaximumSize(new java.awt.Dimension(75, 33));
        F10.setMinimumSize(new java.awt.Dimension(75, 33));
        F10.setPreferredSize(new java.awt.Dimension(75, 33));
        F10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F11.setToolTipText("Control-1 / F11");
        F11.setFocusable(false);
        F11.setMaximumSize(new java.awt.Dimension(75, 33));
        F11.setMinimumSize(new java.awt.Dimension(75, 33));
        F11.setPreferredSize(new java.awt.Dimension(75, 33));
        F11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F0.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F0.setToolTipText("Alt-0 / ~");
        F0.setFocusable(false);
        F0.setMaximumSize(new java.awt.Dimension(75, 33));
        F0.setMinimumSize(new java.awt.Dimension(75, 33));
        F0.setPreferredSize(new java.awt.Dimension(75, 33));
        F0.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F3.setToolTipText("Alt-3 / F3");
        F3.setFocusable(false);
        F3.setMaximumSize(new java.awt.Dimension(75, 33));
        F3.setMinimumSize(new java.awt.Dimension(75, 33));
        F3.setPreferredSize(new java.awt.Dimension(75, 33));
        F3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F1.setToolTipText("Alt-1 / F1");
        F1.setFocusable(false);
        F1.setMaximumSize(new java.awt.Dimension(75, 33));
        F1.setMinimumSize(new java.awt.Dimension(75, 33));
        F1.setPreferredSize(new java.awt.Dimension(75, 33));
        F1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F2.setToolTipText("Alt-2 / F2");
        F2.setFocusable(false);
        F2.setMaximumSize(new java.awt.Dimension(75, 33));
        F2.setMinimumSize(new java.awt.Dimension(75, 33));
        F2.setPreferredSize(new java.awt.Dimension(75, 33));
        F2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F4.setToolTipText("Alt-4 / F4");
        F4.setFocusable(false);
        F4.setMaximumSize(new java.awt.Dimension(75, 33));
        F4.setMinimumSize(new java.awt.Dimension(75, 33));
        F4.setPreferredSize(new java.awt.Dimension(75, 33));
        F4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F5.setToolTipText("Alt-5 / F5");
        F5.setFocusable(false);
        F5.setMaximumSize(new java.awt.Dimension(75, 33));
        F5.setMinimumSize(new java.awt.Dimension(75, 33));
        F5.setPreferredSize(new java.awt.Dimension(75, 33));
        F5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F6.setToolTipText("Alt-6 / F6");
        F6.setFocusable(false);
        F6.setMaximumSize(new java.awt.Dimension(75, 33));
        F6.setMinimumSize(new java.awt.Dimension(75, 33));
        F6.setPreferredSize(new java.awt.Dimension(75, 33));
        F6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f0Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f0Label.setForeground(new java.awt.Color(0, 0, 115));
        f0Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f0Label.setText("F0");

        f4Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f4Label.setForeground(new java.awt.Color(0, 0, 115));
        f4Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f4Label.setText("F4");

        f8Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f8Label.setForeground(new java.awt.Color(0, 0, 115));
        f8Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f8Label.setText("F8");

        f6Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f6Label.setForeground(new java.awt.Color(0, 0, 115));
        f6Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f6Label.setText("F6");

        f1Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f1Label.setForeground(new java.awt.Color(0, 0, 115));
        f1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f1Label.setText("F1");

        f3Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f3Label.setForeground(new java.awt.Color(0, 0, 115));
        f3Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f3Label.setText("F3");

        f5Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f5Label.setForeground(new java.awt.Color(0, 0, 115));
        f5Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f5Label.setText("F5");

        f7Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f7Label.setForeground(new java.awt.Color(0, 0, 115));
        f7Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f7Label.setText("F7");

        f10Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f10Label.setForeground(new java.awt.Color(0, 0, 115));
        f10Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f10Label.setText("F10");

        f9Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f9Label.setForeground(new java.awt.Color(0, 0, 115));
        f9Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f9Label.setText("F9");

        f11Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f11Label.setForeground(new java.awt.Color(0, 0, 115));
        f11Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f11Label.setText("F11");

        f2Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f2Label.setForeground(new java.awt.Color(0, 0, 115));
        f2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f2Label.setText("F2");

        f12Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f12Label.setForeground(new java.awt.Color(0, 0, 115));
        f12Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f12Label.setText("F12");

        F12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F12.setToolTipText("Control-2 / F12");
        F12.setFocusable(false);
        F12.setMaximumSize(new java.awt.Dimension(75, 33));
        F12.setMinimumSize(new java.awt.Dimension(75, 33));
        F12.setPreferredSize(new java.awt.Dimension(75, 33));
        F12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f13Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f13Label.setForeground(new java.awt.Color(0, 0, 115));
        f13Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f13Label.setText("F13");

        F13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F13.setToolTipText("Control-3");
        F13.setFocusable(false);
        F13.setMaximumSize(new java.awt.Dimension(75, 33));
        F13.setMinimumSize(new java.awt.Dimension(75, 33));
        F13.setPreferredSize(new java.awt.Dimension(75, 33));
        F13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f14Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f14Label.setForeground(new java.awt.Color(0, 0, 115));
        f14Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f14Label.setText("F14");

        F14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F14.setToolTipText("Control-4");
        F14.setFocusable(false);
        F14.setMaximumSize(new java.awt.Dimension(75, 33));
        F14.setMinimumSize(new java.awt.Dimension(75, 33));
        F14.setPreferredSize(new java.awt.Dimension(75, 33));
        F14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f15Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f15Label.setForeground(new java.awt.Color(0, 0, 115));
        f15Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f15Label.setText("F15");

        F15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F15.setToolTipText("Control-5");
        F15.setFocusable(false);
        F15.setMaximumSize(new java.awt.Dimension(75, 33));
        F15.setMinimumSize(new java.awt.Dimension(75, 33));
        F15.setPreferredSize(new java.awt.Dimension(75, 33));
        F15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F16.setToolTipText("Control-6");
        F16.setFocusable(false);
        F16.setMaximumSize(new java.awt.Dimension(75, 33));
        F16.setMinimumSize(new java.awt.Dimension(75, 33));
        F16.setPreferredSize(new java.awt.Dimension(75, 33));
        F16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F17.setToolTipText("Control-7");
        F17.setFocusable(false);
        F17.setMaximumSize(new java.awt.Dimension(75, 33));
        F17.setMinimumSize(new java.awt.Dimension(75, 33));
        F17.setPreferredSize(new java.awt.Dimension(75, 33));
        F17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F18.setToolTipText("Control-8");
        F18.setFocusable(false);
        F18.setMaximumSize(new java.awt.Dimension(75, 33));
        F18.setMinimumSize(new java.awt.Dimension(75, 33));
        F18.setPreferredSize(new java.awt.Dimension(75, 33));
        F18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f16Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f16Label.setForeground(new java.awt.Color(0, 0, 115));
        f16Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f16Label.setText("F16");

        f17Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f17Label.setForeground(new java.awt.Color(0, 0, 115));
        f17Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f17Label.setText("F17");

        f18Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f18Label.setForeground(new java.awt.Color(0, 0, 115));
        f18Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f18Label.setText("F18");

        F19.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F19.setToolTipText("Control-9");
        F19.setFocusable(false);
        F19.setMaximumSize(new java.awt.Dimension(75, 33));
        F19.setMinimumSize(new java.awt.Dimension(75, 33));
        F19.setPreferredSize(new java.awt.Dimension(75, 33));
        F19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f19Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f19Label.setForeground(new java.awt.Color(0, 0, 115));
        f19Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f19Label.setText("F19");

        javax.swing.GroupLayout functionPanelLayout = new javax.swing.GroupLayout(functionPanel);
        functionPanel.setLayout(functionPanelLayout);
        functionPanelLayout.setHorizontalGroup(
            functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(functionPanelLayout.createSequentialGroup()
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(functionPanelLayout.createSequentialGroup()
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(f8Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(f0Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(F8, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(F4, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(F0, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(f4Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f5Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(f9Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(f1Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F9, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(F5, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(F1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, functionPanelLayout.createSequentialGroup()
                                .addComponent(F2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(F3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, functionPanelLayout.createSequentialGroup()
                                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(F6, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(f2Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(12, 12, 12)
                                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(F7, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(f3Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, functionPanelLayout.createSequentialGroup()
                                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(f10Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(F10, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(F11, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(f11Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(functionPanelLayout.createSequentialGroup()
                                .addComponent(f6Label, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(f7Label, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, functionPanelLayout.createSequentialGroup()
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(f12Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F12, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f13Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F13, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(functionPanelLayout.createSequentialGroup()
                                .addComponent(F14, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(F15, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(functionPanelLayout.createSequentialGroup()
                                .addComponent(f14Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(12, 12, 12)
                                .addComponent(f15Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, functionPanelLayout.createSequentialGroup()
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(F16, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(f16Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f17Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F17, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(functionPanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(F18, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(F19, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(functionPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(f18Label, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(f19Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        functionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {F0, F1, F10, F11, F2, F3, F4, F5, F6, F7, F8, F9});

        functionPanelLayout.setVerticalGroup(
            functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(functionPanelLayout.createSequentialGroup()
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f0Label)
                    .addComponent(f1Label)
                    .addComponent(f3Label)
                    .addComponent(f2Label))
                .addGap(0, 0, 0)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f4Label)
                    .addComponent(f5Label)
                    .addComponent(f7Label)
                    .addComponent(f6Label))
                .addGap(0, 0, 0)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(f10Label)
                        .addComponent(f9Label)
                        .addComponent(f11Label))
                    .addComponent(f8Label))
                .addGap(0, 0, 0)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f12Label)
                    .addComponent(f14Label)
                    .addComponent(f13Label)
                    .addComponent(f15Label))
                .addGap(0, 0, 0)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(functionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f16Label)
                    .addComponent(f18Label)
                    .addComponent(f17Label)
                    .addComponent(f19Label)))
        );

        FunctionTabs.addTab("F0-F19", functionPanel);

        F20AndUpPanel.setBackground(new java.awt.Color(255, 255, 255));

        f23Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f23Label.setForeground(new java.awt.Color(0, 0, 115));
        f23Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f23Label.setText("F23");

        f20Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f20Label.setForeground(new java.awt.Color(0, 0, 115));
        f20Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f20Label.setText("F20");

        F22.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F22.setToolTipText("Control-Alt-2");
        F22.setFocusable(false);
        F22.setMaximumSize(new java.awt.Dimension(75, 33));
        F22.setMinimumSize(new java.awt.Dimension(75, 33));
        F22.setPreferredSize(new java.awt.Dimension(75, 33));
        F22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F23.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F23.setToolTipText("Control-Alt-3");
        F23.setFocusable(false);
        F23.setMaximumSize(new java.awt.Dimension(75, 33));
        F23.setMinimumSize(new java.awt.Dimension(75, 33));
        F23.setPreferredSize(new java.awt.Dimension(75, 33));
        F23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f22Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f22Label.setForeground(new java.awt.Color(0, 0, 115));
        f22Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f22Label.setText("F22");

        f21Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f21Label.setForeground(new java.awt.Color(0, 0, 115));
        f21Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f21Label.setText("F21");

        F20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F20.setToolTipText("Control-Alt-0");
        F20.setFocusable(false);
        F20.setMaximumSize(new java.awt.Dimension(75, 33));
        F20.setMinimumSize(new java.awt.Dimension(75, 33));
        F20.setPreferredSize(new java.awt.Dimension(75, 33));
        F20.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F21.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F21.setToolTipText("Control-Alt-1");
        F21.setFocusable(false);
        F21.setMaximumSize(new java.awt.Dimension(75, 33));
        F21.setMinimumSize(new java.awt.Dimension(75, 33));
        F21.setPreferredSize(new java.awt.Dimension(75, 33));
        F21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f27Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f27Label.setForeground(new java.awt.Color(0, 0, 115));
        f27Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f27Label.setText("F27");

        f29Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f29Label.setForeground(new java.awt.Color(0, 0, 115));
        f29Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f29Label.setText("F29");

        F24.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F24.setToolTipText("Control-Alt-4");
        F24.setFocusable(false);
        F24.setMaximumSize(new java.awt.Dimension(75, 33));
        F24.setMinimumSize(new java.awt.Dimension(75, 33));
        F24.setPreferredSize(new java.awt.Dimension(75, 33));
        F24.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f30Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f30Label.setForeground(new java.awt.Color(0, 0, 115));
        f30Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f30Label.setText("F30");

        F27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F27.setToolTipText("Control-Alt-7");
        F27.setFocusable(false);
        F27.setMaximumSize(new java.awt.Dimension(75, 33));
        F27.setMinimumSize(new java.awt.Dimension(75, 33));
        F27.setPreferredSize(new java.awt.Dimension(75, 33));
        F27.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f31Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f31Label.setForeground(new java.awt.Color(0, 0, 115));
        f31Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f31Label.setText("F31");

        f24Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f24Label.setForeground(new java.awt.Color(0, 0, 115));
        f24Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f24Label.setText("F24");

        F25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F25.setToolTipText("Control-Alt-5");
        F25.setFocusable(false);
        F25.setMaximumSize(new java.awt.Dimension(75, 33));
        F25.setMinimumSize(new java.awt.Dimension(75, 33));
        F25.setPreferredSize(new java.awt.Dimension(75, 33));
        F25.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F26.setToolTipText("Control-Alt-6");
        F26.setFocusable(false);
        F26.setMaximumSize(new java.awt.Dimension(75, 33));
        F26.setMinimumSize(new java.awt.Dimension(75, 33));
        F26.setPreferredSize(new java.awt.Dimension(75, 33));
        F26.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        F28.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F28.setToolTipText("Control-Alt-8");
        F28.setFocusable(false);
        F28.setMaximumSize(new java.awt.Dimension(75, 33));
        F28.setMinimumSize(new java.awt.Dimension(75, 33));
        F28.setPreferredSize(new java.awt.Dimension(75, 33));
        F28.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f28Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f28Label.setForeground(new java.awt.Color(0, 0, 115));
        f28Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f28Label.setText("F28");

        F29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F29.setToolTipText("Control-Alt-9");
        F29.setFocusable(false);
        F29.setMaximumSize(new java.awt.Dimension(75, 33));
        F29.setMinimumSize(new java.awt.Dimension(75, 33));
        F29.setPreferredSize(new java.awt.Dimension(75, 33));
        F29.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f26Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f26Label.setForeground(new java.awt.Color(0, 0, 115));
        f26Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f26Label.setText("F26");

        F30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F30.setFocusable(false);
        F30.setMaximumSize(new java.awt.Dimension(75, 33));
        F30.setMinimumSize(new java.awt.Dimension(75, 33));
        F30.setPreferredSize(new java.awt.Dimension(75, 33));
        F30.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        f25Label.setFont(new java.awt.Font("Segoe UI Semibold", 0, 13)); // NOI18N
        f25Label.setForeground(new java.awt.Color(0, 0, 115));
        f25Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        f25Label.setText("F25");

        F31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        F31.setFocusable(false);
        F31.setMaximumSize(new java.awt.Dimension(75, 33));
        F31.setMinimumSize(new java.awt.Dimension(75, 33));
        F31.setPreferredSize(new java.awt.Dimension(75, 33));
        F31.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                EditFunction(evt);
            }
        });
        F31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProcessFunction(evt);
            }
        });

        javax.swing.GroupLayout F20AndUpPanelLayout = new javax.swing.GroupLayout(F20AndUpPanel);
        F20AndUpPanel.setLayout(F20AndUpPanelLayout);
        F20AndUpPanelLayout.setHorizontalGroup(
            F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(F20AndUpPanelLayout.createSequentialGroup()
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(F20AndUpPanelLayout.createSequentialGroup()
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(f20Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F20, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f21Label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F21, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(f22Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F22, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(F23, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(f23Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, F20AndUpPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(f24Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(F28, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(F24, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(f28Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(f29Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(F29, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(F25, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(f25Label, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(F20AndUpPanelLayout.createSequentialGroup()
                                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(f30Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(F30, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(f26Label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12)
                                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(F31, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(f27Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(f31Label, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(F20AndUpPanelLayout.createSequentialGroup()
                                .addComponent(F26, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(F27, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        F20AndUpPanelLayout.setVerticalGroup(
            F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(F20AndUpPanelLayout.createSequentialGroup()
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f20Label)
                    .addComponent(f22Label)
                    .addComponent(f21Label)
                    .addComponent(f23Label))
                .addGap(0, 0, 0)
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f24Label)
                    .addComponent(f26Label)
                    .addComponent(f25Label)
                    .addComponent(f27Label))
                .addGap(0, 0, 0)
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(F28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(F31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(F20AndUpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(f30Label)
                        .addComponent(f29Label)
                        .addComponent(f31Label))
                    .addComponent(f28Label))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        FunctionTabs.addTab("F20-F31", F20AndUpPanel);

        javax.swing.GroupLayout LocFunctionsPanelLayout = new javax.swing.GroupLayout(LocFunctionsPanel);
        LocFunctionsPanel.setLayout(LocFunctionsPanelLayout);
        LocFunctionsPanelLayout.setHorizontalGroup(
            LocFunctionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LocFunctionsPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(LocFunctionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(CurrentKeyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, LocFunctionsPanelLayout.createSequentialGroup()
                        .addComponent(PowerOff, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(OnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ActiveLocLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(LocFunctionsPanelLayout.createSequentialGroup()
                        .addComponent(Backward, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(Forward, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(SpeedSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FunctionTabs, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locIcon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        LocFunctionsPanelLayout.setVerticalGroup(
            LocFunctionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LocFunctionsPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(LocFunctionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PowerOff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OnButton))
                .addGap(4, 4, 4)
                .addComponent(CurrentKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ActiveLocLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(LocFunctionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Backward)
                    .addComponent(Forward))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(locIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FunctionTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1))
        );

        PowerOff.getAccessibleContext().setAccessibleName("");
        ActiveLocLabel.getAccessibleContext().setAccessibleName("LocomotiveNameBig");

        mainMenuBar.setFocusable(false);
        mainMenuBar.setMaximumSize(new java.awt.Dimension(1200, 30));
        mainMenuBar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mainMenuBarKeyPressed(evt);
            }
        });

        fileMenu.setText("File");
        fileMenu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fileMenuKeyPressed(evt);
            }
        });

        backupDataMenuItem.setText("Backup TrainControl Data");
        backupDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backupDataMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(backupDataMenuItem);

        changeIPMenuItem.setText("Change Central Station IP");
        changeIPMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeIPMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(changeIPMenuItem);
        fileMenu.add(jSeparator17);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        locomotiveMenu.setText("Locomotives");
        locomotiveMenu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                locomotiveMenuKeyPressed(evt);
            }
        });

        quickFindMenuItem.setText("Quick Find Locomotive");
        quickFindMenuItem.setToolTipText("Control+F");
        quickFindMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickFindMenuItemActionPerformed(evt);
            }
        });
        locomotiveMenu.add(quickFindMenuItem);
        locomotiveMenu.add(jSeparator7);

        viewDatabaseMenuItem.setText("Browse Database");
        viewDatabaseMenuItem.setToolTipText("Control+A");
        viewDatabaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDatabaseMenuItemActionPerformed(evt);
            }
        });
        locomotiveMenu.add(viewDatabaseMenuItem);

        addLocomotiveMenuItem.setText("Add Locomotive");
        addLocomotiveMenuItem.setToolTipText("Control+D");
        addLocomotiveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocomotiveMenuItemActionPerformed(evt);
            }
        });
        locomotiveMenu.add(addLocomotiveMenuItem);
        locomotiveMenu.add(jSeparator6);

        syncMenuItem.setText("Sync Database w/ Central Station");
        syncMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncMenuItemActionPerformed(evt);
            }
        });
        locomotiveMenu.add(syncMenuItem);

        mainMenuBar.add(locomotiveMenu);

        functionsMenu.setText("Functions");
        functionsMenu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                functionsMenuKeyReleased(evt);
            }
        });

        turnOnLightsMenuItem.setText("Turn On All Lights");
        turnOnLightsMenuItem.setToolTipText("Turns on lights for all mapped locomotives.");
        turnOnLightsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                turnOnLightsMenuItemActionPerformed(evt);
            }
        });
        functionsMenu.add(turnOnLightsMenuItem);

        turnOffFunctionsMenuItem.setText("Turn Off All Functions");
        turnOffFunctionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                turnOffFunctionsMenuItemActionPerformed(evt);
            }
        });
        functionsMenu.add(turnOffFunctionsMenuItem);
        functionsMenu.add(jSeparator16);

        syncFullLocStateMenuItem.setText("Sync Full Function State w/ Central Station");
        syncFullLocStateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncFullLocStateMenuItemActionPerformed(evt);
            }
        });
        functionsMenu.add(syncFullLocStateMenuItem);

        mainMenuBar.add(functionsMenu);

        layoutMenu.setText("Layouts");
        layoutMenu.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                layoutMenuKeyPressed(evt);
            }
        });

        showCurrentLayoutFolderMenuItem.setText("Show Current Data Source");
        showCurrentLayoutFolderMenuItem.setToolTipText("Shows where the layout is being loaded from.");
        showCurrentLayoutFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCurrentLayoutFolderMenuItemActionPerformed(evt);
            }
        });
        layoutMenu.add(showCurrentLayoutFolderMenuItem);
        layoutMenu.add(jSeparator5);

        chooseLocalDataFolderMenuItem.setText("Open Layout");
        chooseLocalDataFolderMenuItem.setToolTipText("Select the location of your existing layout files on your computer.  Lets you switch between diagrams.");
        chooseLocalDataFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseLocalDataFolderMenuItemActionPerformed(evt);
            }
        });
        layoutMenu.add(chooseLocalDataFolderMenuItem);

        modifyLocalLayoutMenu.setText("Modify Current Layout");
        modifyLocalLayoutMenu.setToolTipText("Lets you change the track diagram.");

        addBlankPageMenuItem.setText("Add Blank Page");
        addBlankPageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBlankPageMenuItemActionPerformed(evt);
            }
        });
        modifyLocalLayoutMenu.add(addBlankPageMenuItem);

        renameLayoutMenuItem.setText("Rename Current Page");
        renameLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameLayoutMenuItemActionPerformed(evt);
            }
        });
        modifyLocalLayoutMenu.add(renameLayoutMenuItem);

        duplicateLayoutMenuItem.setText("Duplicate Current Page");
        duplicateLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateLayoutMenuItemActionPerformed(evt);
            }
        });
        modifyLocalLayoutMenu.add(duplicateLayoutMenuItem);
        modifyLocalLayoutMenu.add(jSeparator22);

        editCurrentPageActionPerformed.setText("Edit Current Page");
        editCurrentPageActionPerformed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCurrentPageActionPerformedActionPerformed(evt);
            }
        });
        modifyLocalLayoutMenu.add(editCurrentPageActionPerformed);

        openLegacyTrackDiagramEditor.setText("Edit w/ Legacy Editor");
        openLegacyTrackDiagramEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openLegacyTrackDiagramEditorActionPerformed(evt);
            }
        });
        modifyLocalLayoutMenu.add(openLegacyTrackDiagramEditor);
        modifyLocalLayoutMenu.add(jSeparator21);

        deleteLayoutMenuItem.setText("Delete Current Page");
        deleteLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLayoutMenuItemActionPerformed(evt);
            }
        });
        modifyLocalLayoutMenu.add(deleteLayoutMenuItem);

        layoutMenu.add(modifyLocalLayoutMenu);

        initializeLocalLayoutMenuItem.setText("Create New Layout");
        initializeLocalLayoutMenuItem.setToolTipText("Creates a blank track diagram that you can edit visually.");
        initializeLocalLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initializeLocalLayoutMenuItemActionPerformed(evt);
            }
        });
        layoutMenu.add(initializeLocalLayoutMenuItem);
        layoutMenu.add(jSeparator8);

        switchCSLayoutMenuItem.setText("Switch to Central Station Layout");
        switchCSLayoutMenuItem.setToolTipText("Reverts to using the track diagram on your Central Station, if any.");
        switchCSLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchCSLayoutMenuItemActionPerformed(evt);
            }
        });
        layoutMenu.add(switchCSLayoutMenuItem);

        downloadCSLayoutMenuItem.setText("Download Central Station Layout");
        downloadCSLayoutMenuItem.setToolTipText("Save the Central Station's layout to the local filesystem.");
        downloadCSLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadCSLayoutMenuItemActionPerformed(evt);
            }
        });
        layoutMenu.add(downloadCSLayoutMenuItem);

        openCS3AppMenuItem.setText("Open CS3 Web App");
        openCS3AppMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCS3AppMenuItemActionPerformed(evt);
            }
        });
        layoutMenu.add(openCS3AppMenuItem);

        mainMenuBar.add(layoutMenu);

        routesMenu.setText("Routes");

        exportRoutesMenuItem.setText("Export");
        exportRoutesMenuItem.setToolTipText("Exports all current routes into a file.");
        exportRoutesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportRoutesMenuItemActionPerformed(evt);
            }
        });
        routesMenu.add(exportRoutesMenuItem);

        importRoutesMenuItem.setText("Import");
        importRoutesMenuItem.setToolTipText("Replace all existing routes with those from a file.");
        importRoutesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importRoutesMenuItemActionPerformed(evt);
            }
        });
        routesMenu.add(importRoutesMenuItem);

        mainMenuBar.add(routesMenu);

        interfaceMenu.setText("Preferences");

        windowAlwaysOnTopMenuItem.setSelected(true);
        windowAlwaysOnTopMenuItem.setText("Window Always on Top");
        windowAlwaysOnTopMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowAlwaysOnTopMenuItemActionPerformed(evt);
            }
        });
        interfaceMenu.add(windowAlwaysOnTopMenuItem);

        rememberLocationMenuItem.setSelected(true);
        rememberLocationMenuItem.setText("Remember Window Locations");
        rememberLocationMenuItem.setToolTipText("Restore program windows in the same place on startup?");
        rememberLocationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rememberLocationMenuItemActionPerformed(evt);
            }
        });
        interfaceMenu.add(rememberLocationMenuItem);
        interfaceMenu.add(jSeparator19);

        locomotiveControlMenu.setText("Locomotive Control");

        slidersChangeActiveLocMenuItem.setSelected(true);
        slidersChangeActiveLocMenuItem.setText("Sliders Change Active Loc");
        slidersChangeActiveLocMenuItem.setToolTipText("Change the active locomotive when using the sliders on the Locomotive Control page.");
        slidersChangeActiveLocMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slidersChangeActiveLocMenuItemActionPerformed(evt);
            }
        });
        locomotiveControlMenu.add(slidersChangeActiveLocMenuItem);

        showKeyboardHintsMenuItem.setSelected(true);
        showKeyboardHintsMenuItem.setText("Show Keyboard Control Hints");
        showKeyboardHintsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showKeyboardHintsMenuItemActionPerformed(evt);
            }
        });
        locomotiveControlMenu.add(showKeyboardHintsMenuItem);

        activeLocInTitle.setSelected(true);
        activeLocInTitle.setText("Active Locomotive in Popup Titles");
        activeLocInTitle.setToolTipText("Shows the name of the active locomotive in popup windows.");
        activeLocInTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeLocInTitleActionPerformed(evt);
            }
        });
        locomotiveControlMenu.add(activeLocInTitle);
        locomotiveControlMenu.add(jSeparator20);

        buttonGroup3.add(keyboardQwertyMenuItem);
        keyboardQwertyMenuItem.setSelected(true);
        keyboardQwertyMenuItem.setText("QWERTY Keyboard");
        keyboardQwertyMenuItem.setToolTipText("Select the type of keyboard your computer has.");
        keyboardQwertyMenuItem.setActionCommand("0");
        keyboardQwertyMenuItem.setName("0"); // NOI18N
        keyboardQwertyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardQwertyMenuItemActionPerformed(evt);
            }
        });
        locomotiveControlMenu.add(keyboardQwertyMenuItem);

        buttonGroup3.add(keyboardQwertzMenuItem);
        keyboardQwertzMenuItem.setText("QWERTZ Keyboard");
        keyboardQwertzMenuItem.setToolTipText("Select the type of keyboard your computer has.");
        keyboardQwertzMenuItem.setActionCommand("1");
        keyboardQwertzMenuItem.setName("1"); // NOI18N
        keyboardQwertzMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardQwertyMenuItemActionPerformed(evt);
            }
        });
        locomotiveControlMenu.add(keyboardQwertzMenuItem);

        buttonGroup3.add(keyboardAzertyMenuItem);
        keyboardAzertyMenuItem.setText("AZERTY Keyboard");
        keyboardAzertyMenuItem.setToolTipText("Select the type of keyboard your computer has.");
        keyboardAzertyMenuItem.setActionCommand("2");
        keyboardAzertyMenuItem.setName("2"); // NOI18N
        keyboardAzertyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyboardQwertyMenuItemActionPerformed(evt);
            }
        });
        locomotiveControlMenu.add(keyboardAzertyMenuItem);

        interfaceMenu.add(locomotiveControlMenu);

        jMenu1.setText("Startup");

        buttonGroup4.add(powerOnStartup);
        powerOnStartup.setSelected(true);
        powerOnStartup.setText("Power On");
        powerOnStartup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerOnStartupActionPerformed(evt);
            }
        });
        jMenu1.add(powerOnStartup);

        buttonGroup4.add(powerOffStartup);
        powerOffStartup.setText("Power Off");
        powerOffStartup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerOffStartupActionPerformed(evt);
            }
        });
        jMenu1.add(powerOffStartup);

        buttonGroup4.add(powerNoChangeStartup);
        powerNoChangeStartup.setText("Do Nothing");
        powerNoChangeStartup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerNoChangeStartupActionPerformed(evt);
            }
        });
        jMenu1.add(powerNoChangeStartup);
        jMenu1.add(jSeparator18);

        AutoLoadAutonomyMenuItem.setSelected(true);
        AutoLoadAutonomyMenuItem.setText("Load Autonomy");
        AutoLoadAutonomyMenuItem.setToolTipText("Attempts to parse the autonomy graph at startup.");
        AutoLoadAutonomyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AutoLoadAutonomyMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(AutoLoadAutonomyMenuItem);
        jMenu1.add(jSeparator3);

        checkForUpdates.setSelected(true);
        checkForUpdates.setText("Check for Updates");
        checkForUpdates.setToolTipText("At startup, should we check for a new release of TrainControl?");
        checkForUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkForUpdatesActionPerformed(evt);
            }
        });
        jMenu1.add(checkForUpdates);

        interfaceMenu.add(jMenu1);

        layoutMenuItem.setText("Layouts");

        menuItemShowLayoutAddresses.setSelected(true);
        menuItemShowLayoutAddresses.setText("Show Addresses");
        menuItemShowLayoutAddresses.setToolTipText("Show accessory address labels in the track diagram?");
        menuItemShowLayoutAddresses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemShowLayoutAddressesActionPerformed(evt);
            }
        });
        layoutMenuItem.add(menuItemShowLayoutAddresses);

        interfaceMenu.add(layoutMenuItem);

        mainMenuBar.add(interfaceMenu);

        helpMenu.setText("Help");

        viewReleasesMenuItem.setText("View Releases");
        viewReleasesMenuItem.setToolTipText("Read about the latest version of TrainControl.");
        viewReleasesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewReleasesMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(viewReleasesMenuItem);

        downloadUpdateMenuItem.setText("Download Update");
        downloadUpdateMenuItem.setToolTipText("Download the update file to your computer.");
        downloadUpdateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadUpdateMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(downloadUpdateMenuItem);

        aboutMenuItem.setText("About / Readme");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(KeyboardTab, javax.swing.GroupLayout.PREFERRED_SIZE, 802, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(LocFunctionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LocFunctionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(KeyboardTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        KeyboardTab.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public ViewListener getModel()
    {
        return model;
    }
    
    private void LocControlPanelKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_LocControlPanelKeyPressed
    {//GEN-HEADEREND:event_LocControlPanelKeyPressed
        int keyCode = evt.getKeyCode();
        boolean altPressed = (evt.getModifiers() & KeyEvent.ALT_MASK) != 0;
        boolean controlPressed = (evt.getModifiers() & KeyEvent.CTRL_MASK) != 0 || (evt.getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0;
        
        if (altPressed && keyCode == KeyEvent.VK_G)
        {
            go();
        }
        else if (altPressed && keyCode == KeyEvent.VK_P)
        {
            this.applyPreferredFunctions(this.activeLoc);
        }
        else if (altPressed && keyCode == KeyEvent.VK_V)
        {
            this.applyPreferredSpeed(this.activeLoc);
        }
        else if (altPressed && keyCode == KeyEvent.VK_O)
        {
            this.locFunctionsOff(this.activeLoc);
        }
        else if (altPressed && keyCode == KeyEvent.VK_S)
        {
            this.savePreferredFunctions(this.activeLoc);
        }
        else if (altPressed && keyCode == KeyEvent.VK_U)
        {
            this.savePreferredSpeed(this.activeLoc);
        }
        else if (controlPressed && keyCode == KeyEvent.VK_A)
        {
            this.selectLocomotiveActivated(this.currentButton);
        }
        else if (controlPressed && keyCode == KeyEvent.VK_F)
        {
            this.quickLocSearch();
        }
        else if (controlPressed && keyCode == KeyEvent.VK_C)
        {
            this.setCopyTarget(this.currentButton, false);
        }
        else if (controlPressed && keyCode == KeyEvent.VK_D)
        {
            this.getLocAdder().setVisible(true);
        }
        else if (controlPressed && keyCode == KeyEvent.VK_N)
        {
            this.changeLocNotes(this.getButtonLocomotive(this.currentButton));
        }
        else if (controlPressed && keyCode == KeyEvent.VK_L)
        {
            this.changeLinkedLocomotives((MarklinLocomotive) this.getButtonLocomotive(this.currentButton));
        }
        else if (controlPressed && keyCode == KeyEvent.VK_M)
        {
            this.toggleMenuBar.setSelected(!this.toggleMenuBar.isSelected());
            toggleMenuBarActionPerformed(null);
        }
        else if (controlPressed && keyCode == KeyEvent.VK_DELETE)
        {
            if (this.getButtonLocomotive(this.currentButton) != null)
            {
                this.deleteLoc(this.getButtonLocomotive(this.currentButton).getName());
            }
        }
        else if (controlPressed && keyCode == KeyEvent.VK_R)
        {
            this.changeLocAddress((MarklinLocomotive) this.getButtonLocomotive(this.currentButton));
        }
        else if (controlPressed && keyCode == KeyEvent.VK_V) // Paste
        {
            if (this.hasCopyTarget())
            {
                this.doPaste(this.currentButton, false, false);
            }
        }
        else if (controlPressed && keyCode == KeyEvent.VK_S) // Swap
        {
            if (this.hasCopyTarget())
            {
                this.doPaste(this.currentButton, true, false);
            }
        }
        else if (controlPressed && keyCode == KeyEvent.VK_X)
        {
            this.setCopyTarget(this.currentButton, true);
        }
        else if (this.buttonMapping.containsKey(keyCode))
        {
            this.displayCurrentButtonLoc(this.buttonMapping.get(evt.getKeyCode()));
        }
        else if (keyCode == KeyEvent.VK_UP)
        {            
            if (altPressed)
            {
                incrementLocSpeed(SPEED_STEP * 2);
            }
            else if (controlPressed)
            {
                incrementLocSpeed(1);
            }
            else
            {
                this.UpArrowLetterButtonPressed(null);
            }
        }
        else if (keyCode == KeyEvent.VK_DOWN)
        {            
            if (altPressed)
            {
                decrementLocSpeed(SPEED_STEP * 2);
            }
            else if (controlPressed)
            {
                decrementLocSpeed(1);
            }
            else
            {
                this.DownArrowLetterButtonPressed(null);
            }
        }
        else if (keyCode == KeyEvent.VK_RIGHT && !altPressed)
        {
            if (controlPressed)
            {
                this.forwardLoc();
            }
            else
            {
                this.RightArrowLetterButtonPressed(null);
            }
        }
        else if (keyCode == KeyEvent.VK_LEFT && !altPressed)
        {
            if (controlPressed)
            {
                this.backwardLoc();
            }
            else
            {
               this.LeftArrowLetterButtonPressed(null); 
            }
        }
        else if (keyCode == KeyEvent.VK_SPACE)
        {
            this.SpacebarButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_0 && !altPressed && !controlPressed)
        {
            this.ZeroButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_1 && !altPressed && !controlPressed)
        {
            this.OneButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_2 && !altPressed && !controlPressed)
        {
            this.TwoButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_3 && !altPressed && !controlPressed)
        {
            this.ThreeButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_4 && !altPressed && !controlPressed)
        {
            this.FourButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_5 && !altPressed && !controlPressed)
        {
            this.FiveButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_6 && !altPressed && !controlPressed)
        {
            this.SixButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_7 && !altPressed && !controlPressed)
        {
            this.SevenButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_8 && !altPressed && !controlPressed)
        {
            this.EightButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_9 && !altPressed && !controlPressed)
        {
            this.NineButtonActionPerformed(null);
        }
        else if (keyCode == KeyEvent.VK_MINUS || keyCode == KeyEvent.VK_UNDERSCORE || keyCode == KeyEvent.VK_QUOTE || keyCode == KeyEvent.VK_OPEN_BRACKET)
        {
            if (this.KeyboardTab.getSelectedIndex() == 1)
            {
                int currentIndex = this.LayoutList.getSelectedIndex();
                
                if (currentIndex > 0)
                {
                     this.LayoutList.setSelectedIndex(currentIndex - 1);
                     //this.repaintLayout();
                }
            }
            else
            {
                this.PrevKeyboardActionPerformed(null);
            }
        }
        else if (keyCode == KeyEvent.VK_EQUALS || keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_LEFT_PARENTHESIS || keyCode == KeyEvent.VK_CLOSE_BRACKET)
        {
            if (this.KeyboardTab.getSelectedIndex() == 1)
            {
                int currentIndex = this.LayoutList.getSelectedIndex();
                
                if (currentIndex < this.LayoutList.getItemCount() - 1)
                {
                     this.LayoutList.setSelectedIndex(currentIndex + 1);
                }
            }
            else
            {
                this.NextKeyboardActionPerformed(null);
            } 
        }
        else if ((keyCode == KeyEvent.VK_COMMA && altPressed) || (keyCode == KeyEvent.VK_SEMICOLON && altPressed))
        {
            this.switchLocMapping(1);    
        }
        else if ((keyCode == KeyEvent.VK_PERIOD && altPressed) || (keyCode == KeyEvent.VK_COLON && altPressed))
        {
            this.switchLocMapping(TrainControlUI.NUM_LOC_MAPPINGS); 
        }
        else if ((keyCode == KeyEvent.VK_COMMA && !altPressed) || (keyCode == KeyEvent.VK_SEMICOLON && !altPressed) || (keyCode == KeyEvent.VK_LEFT && altPressed))
        {
            this.PrevLocMappingActionPerformed(null);   
        }
        else if ((keyCode == KeyEvent.VK_PERIOD && !altPressed) || (keyCode == KeyEvent.VK_COLON && !altPressed) || (keyCode == KeyEvent.VK_RIGHT && altPressed))
        {
            this.NextLocMappingActionPerformed(null);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD0 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_BACK_QUOTE || (keyCode == KeyEvent.VK_0 && altPressed && !controlPressed))
        {
            this.switchF(0);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD1 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F1 || (keyCode == KeyEvent.VK_1 && altPressed && !controlPressed))
        {
            this.switchF(1);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD2 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F2 || (keyCode == KeyEvent.VK_2 && altPressed && !controlPressed))
        {
            this.switchF(2);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD3 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F3 || (keyCode == KeyEvent.VK_3 && altPressed && !controlPressed))
        {
            this.switchF(3);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD4 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F4 || (keyCode == KeyEvent.VK_4 && altPressed && !controlPressed))
        {
            this.switchF(4);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD5 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F5 || (keyCode == KeyEvent.VK_5 && altPressed && !controlPressed))
        {
            this.switchF(5);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD6 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F6 || (keyCode == KeyEvent.VK_6 && altPressed && !controlPressed))
        {
            this.switchF(6);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD7 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F7 || (keyCode == KeyEvent.VK_7 && altPressed && !controlPressed))
        {
            this.switchF(7);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD8 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F8 || (keyCode == KeyEvent.VK_8 && altPressed && !controlPressed))
        {
            this.switchF(8);
        }
        else if ((keyCode == KeyEvent.VK_NUMPAD9 && !controlPressed && !altPressed) || keyCode == KeyEvent.VK_F9 || (keyCode == KeyEvent.VK_9 && altPressed && !controlPressed))
        {
            this.switchF(9);
        }
        else if (keyCode == KeyEvent.VK_F10 || ((keyCode == KeyEvent.VK_0 || keyCode == KeyEvent.VK_NUMPAD0) && !altPressed && controlPressed))
        {
            this.switchF(10);
        }
        else if (keyCode == KeyEvent.VK_F11 || ((keyCode == KeyEvent.VK_1 || keyCode == KeyEvent.VK_NUMPAD1) && !altPressed && controlPressed))
        {
            this.switchF(11);
        }
        else if (keyCode == KeyEvent.VK_F12 || ((keyCode == KeyEvent.VK_2 || keyCode == KeyEvent.VK_NUMPAD2) && !altPressed && controlPressed))
        {
            this.switchF(12);
        }
        else if (keyCode == KeyEvent.VK_F13 || ((keyCode == KeyEvent.VK_3 || keyCode == KeyEvent.VK_NUMPAD3) && !altPressed && controlPressed))
        {
            this.switchF(13);
        }
        else if (keyCode == KeyEvent.VK_F14 || ((keyCode == KeyEvent.VK_4 || keyCode == KeyEvent.VK_NUMPAD4) && !altPressed && controlPressed))
        {
            this.switchF(14);
        }
        else if (keyCode == KeyEvent.VK_F15 || ((keyCode == KeyEvent.VK_5 || keyCode == KeyEvent.VK_NUMPAD5) && !altPressed && controlPressed))
        {
            this.switchF(15);
        }
        else if (keyCode == KeyEvent.VK_F16 || ((keyCode == KeyEvent.VK_6 || keyCode == KeyEvent.VK_NUMPAD6) && !altPressed && controlPressed))
        {
            this.switchF(16);
        }
        else if (keyCode == KeyEvent.VK_F17 || ((keyCode == KeyEvent.VK_7 || keyCode == KeyEvent.VK_NUMPAD7) && !altPressed && controlPressed))
        {
            this.switchF(17);
        }
        else if (keyCode == KeyEvent.VK_F18 || ((keyCode == KeyEvent.VK_8 || keyCode == KeyEvent.VK_NUMPAD8) && !altPressed && controlPressed))
        {
            this.switchF(18);
        }
        else if (keyCode == KeyEvent.VK_F19 || ((keyCode == KeyEvent.VK_9 || keyCode == KeyEvent.VK_NUMPAD9) && !altPressed && controlPressed))
        {
            this.switchF(19);
        }
        else if (keyCode == KeyEvent.VK_F20 || ((keyCode == KeyEvent.VK_0 || keyCode == KeyEvent.VK_NUMPAD0) && altPressed && controlPressed))
        {
            this.switchF(20);
        }
        else if (keyCode == KeyEvent.VK_F21 || ((keyCode == KeyEvent.VK_1 || keyCode == KeyEvent.VK_NUMPAD1) && altPressed && controlPressed))
        {
            this.switchF(21);
        }
        else if (keyCode == KeyEvent.VK_F22 || ((keyCode == KeyEvent.VK_2 || keyCode == KeyEvent.VK_NUMPAD2) && altPressed && controlPressed))
        {
            this.switchF(22);
        }
        else if (keyCode == KeyEvent.VK_F23 || ((keyCode == KeyEvent.VK_3 || keyCode == KeyEvent.VK_NUMPAD3) && altPressed && controlPressed))
        {
            this.switchF(23);
        }
        else if (keyCode == KeyEvent.VK_F24 || ((keyCode == KeyEvent.VK_4 || keyCode == KeyEvent.VK_NUMPAD4) && altPressed && controlPressed))
        {
            this.switchF(24);
        }
        else if ((keyCode == KeyEvent.VK_5 || keyCode == KeyEvent.VK_NUMPAD5) && altPressed && controlPressed)
        {
            this.switchF(25);
        }
        else if ((keyCode == KeyEvent.VK_6 || keyCode == KeyEvent.VK_NUMPAD6) && altPressed && controlPressed)
        {
            this.switchF(26);
        }
        else if ((keyCode == KeyEvent.VK_7 || keyCode == KeyEvent.VK_NUMPAD7) && altPressed && controlPressed)
        {
            this.switchF(27);
        }
        else if ((keyCode == KeyEvent.VK_8 || keyCode == KeyEvent.VK_NUMPAD8) && altPressed && controlPressed)
        {
            this.switchF(28);
        }
        else if ((keyCode == KeyEvent.VK_9 || keyCode == KeyEvent.VK_NUMPAD9) && altPressed && controlPressed)
        {
            this.switchF(29);
        }
        else if (keyCode == KeyEvent.VK_ESCAPE)
        {
            stop();
        }
        else if (keyCode == KeyEvent.VK_SHIFT)
        {
            ShiftButtonActionPerformed(null);
        }  
        else if (keyCode == KeyEvent.VK_ENTER)
        {
            AltEmergencyStopActionPerformed(null);
        }
        else if ((keyCode == KeyEvent.VK_CAPS_LOCK || keyCode == KeyEvent.VK_BACK_SPACE) && !altPressed)
        {
            // Easy tab cycling
            do
            {
                this.KeyboardTab.setSelectedIndex(
                    (this.KeyboardTab.getSelectedIndex() + 1) 
                        % this.KeyboardTab.getComponentCount()
                );
            }
            while (!this.KeyboardTab.isEnabledAt(this.KeyboardTab.getSelectedIndex()));
        } 
        else if ((keyCode == KeyEvent.VK_CAPS_LOCK || keyCode == KeyEvent.VK_BACK_SPACE) && altPressed)
        {
            do
            {
                this.KeyboardTab.setSelectedIndex(
                    Math.floorMod(this.KeyboardTab.getSelectedIndex() - 1, 
                            this.KeyboardTab.getComponentCount()
                    )
                );
            }
            while (!this.KeyboardTab.isEnabledAt(this.KeyboardTab.getSelectedIndex()));
        } 
        else if (keyCode == KeyEvent.VK_SLASH || keyCode == KeyEvent.VK_LESS)
        {
            // Cycle function tabs
            this.FunctionTabs.setSelectedIndex(
                (this.FunctionTabs.getSelectedIndex() + 1) 
                    % this.FunctionTabs.getComponentCount()
            );
        }
    }//GEN-LAST:event_LocControlPanelKeyPressed

    private void WindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_WindowClosed
    {//GEN-HEADEREND:event_WindowClosed
        // Auto-save confirmation
        if (this.autosave.isSelected() && this.model.hasAutoLayout() 
                && this.model.getAutoLayout().isValid()
                && !this.model.getAutoLayout().getPoints().isEmpty())
        {
            if (this.model.getAutoLayout().isRunning())
            {
                gracefulStopActionPerformed(null);
                int dialogResult = JOptionPane.showConfirmDialog(
                        this, "Autonomy logic is still running.  "
                                + "State will not be auto-saved unless all trains are gracefully stopped.  Are you use you want to quit?"
                                , "Confirm Exit", JOptionPane.YES_NO_OPTION);
                
                if(dialogResult == JOptionPane.NO_OPTION) return;
            }
        }
        
        model.saveState(false);
        this.saveState(false);
        //model.stop();
        this.dispose();
        System.exit(0);
    }//GEN-LAST:event_WindowClosed

    /**
     * Shows the tab with the specified icon
     * @param tabIcon 
     */
    public void showTab(Icon tabIcon)
    {
        for (int i = 0; i < this.KeyboardTab.getTabCount(); i++)
        {
            if (tabIcon.equals(this.KeyboardTab.getIconAt(i)))
            {
                this.KeyboardTab.setSelectedIndex(i);
                this.KeyboardTab.requestFocus();
            }
        }
    }
    
    public void deleteRoute(String routeName)
    {
        MarklinRoute route = this.model.getRoute(routeName);
        
        if (route != null)
        {            
            int dialogResult = JOptionPane.showConfirmDialog(RoutePanel, "Delete route " + route.getName() + "?", "Route Deletion", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION)
            {
                this.model.deleteRoute(route.getName());
                refreshRouteList();

                // Ensure route changes are synced
                this.model.syncWithCS2();
                this.repaintLayout();
                this.repaintLoc();
            }
        }
    }
    
    /**
     * Navigates to a specific layout page
     * @param index 
     */
    public void goToLayoutPage(int index)
    {
        int page = index + 1;
        
        if (this.LayoutList.getModel().getSize() > index && index >= 0)
        {
            this.model.log("Jumping to layout page " + page);
            this.LayoutList.setSelectedIndex(index);
            this.repaintLayout();
        }
        else
        {
            this.model.log("Layout page " + page + " does not exist.");
        }
    }
        
    public void executeRoute(String route)
    {
        new Thread(() ->
        {
            this.model.execRoute(route);
            refreshRouteList();
        }).start();
    }
        
    public void childWindowKeyEvent(java.awt.event.KeyEvent evt)
    {
        this.LocControlPanelKeyPressed(evt);
    }
        
    private void ProcessFunction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProcessFunction
        javax.swing.JToggleButton b = (javax.swing.JToggleButton) evt.getSource();

        Integer fNumber = this.functionMapping.get(b);
        Boolean state = b.isSelected();

        this.fireF(fNumber, state);
    }//GEN-LAST:event_ProcessFunction

    private void ForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ForwardActionPerformed
        forwardLoc();
    }//GEN-LAST:event_ForwardActionPerformed

    private void BackwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackwardActionPerformed
        backwardLoc();
    }//GEN-LAST:event_BackwardActionPerformed

    private void MainSpeedSliderClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MainSpeedSliderClicked
        
        if (evt.getClickCount() == 2 && SwingUtilities.isRightMouseButton(evt))
        {
            this.switchDirection();
        }
        else
        {
            setLocSpeed(SpeedSlider.getValue());
        }
    }//GEN-LAST:event_MainSpeedSliderClicked

    private void PowerOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PowerOffActionPerformed
        stop();
    }//GEN-LAST:event_PowerOffActionPerformed

    private void OnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OnButtonActionPerformed
        go();
    }//GEN-LAST:event_OnButtonActionPerformed

    public void doSync(Component c)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            this.syncMenuItem.setEnabled(false);
            this.functionsMenu.setEnabled(false);
            
            new Thread(() ->
            {
                Integer r = this.model.syncWithCS2();
                refreshRouteList();
                this.selector.refreshLocSelectorList();
                this.repaintLoc(true, null);
                this.repaintLayout();

                this.syncMenuItem.setEnabled(true);
                this.functionsMenu.setEnabled(true);

                if ("-1".equals(r.toString()))
                {
                    JOptionPane.showMessageDialog(c, "Sync failed.  See log.");
                }
                else
                {
                    JOptionPane.showMessageDialog(c, "Sync complete.  Items added: " + r.toString());
                }
            }).start();
        }));
    }
    
    /**
     * Puts the current keyboard mappings on the keyboard
     */
    public void copyCurrentPage()
    {
        this.pageToCopy = this.locMappingNumber;
    }
    
    /**
     * Pastes copied mappings to the current page
     */
    public void pasteCopiedPage()
    {
        if (pageCopied())
        {
            int dialogResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to replace the mappings on the current page with those from page " + this.pageToCopy + "?", "Paste Mappings", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION)
            {
                this.locMapping.set(this.locMappingNumber - 1, new HashMap<>(this.locMapping.get(this.pageToCopy - 1)));
                this.pageToCopy = null;
                this.repaintMappings();
                
                // We need to repaint the locomotive if a button on this page was active
                if (currentButtonlocMappingNumber == this.locMappingNumber)
                {
                    this.displayCurrentButtonLoc(this.currentButton);
                }
            }
        }
    }
    
    /**
     * Is there anything on the page clipboard?
     * @return 
     */
    public boolean pageCopied()
    {
        return this.pageToCopy != null;
    }
    
    /**
     * Resets the mappings on the current page
     */
    public void clearCurrentPage()
    {
        new Thread(() ->
        {
            int dialogResult = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all key mappings\non the current page (" + this.locMappingNumber + ")?", "Reset Keyboard Mappings", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION)
            {
                this.activeLoc = null;

                for (JButton key : this.currentLocMapping().keySet())
                {
                    this.currentLocMapping().put(key, null);
                }
                repaintMappings();
                repaintLoc();
            }
        }).start();
    }
    
    /**
     * Prompts for a locomotive name, and activates that locomotive if found
     */
    public void quickLocSearch()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            String input = JOptionPane.showInputDialog(this, "Enter locomotive name to jump to:");

            if (input != null)
            {
                this.jumpToLocomotive(input);
            }
        }));
    }
    
    /**
     * Prompts for a new route ID and attempts to change the ID of the given route
     * @param routeName 
     */
    public void changeRouteId(String routeName)
    {        
        try
        {
            MarklinRoute currentRoute = this.model.getRoute(routeName);
            
            if (currentRoute != null)
            {
                String input = JOptionPane.showInputDialog(this, "Enter new route ID:", currentRoute.getId());

                if (input != null)
                {
                    int newId = Math.abs(Integer.parseInt(input));

                    if (newId != currentRoute.getId())
                    {    
                        if (!this.model.changeRouteId(routeName, newId))
                        {
                            JOptionPane.showMessageDialog(this, "This ID already exists.  Delete the other route first.");
                        }

                        this.model.syncWithCS2();
                        this.repaintLayout();  
                        
                        this.refreshRouteList();
                    }
                }
            }
        }
        catch (HeadlessException | NumberFormatException e)
        {
            JOptionPane.showMessageDialog(this, "ID must be a positive integer.");
        }
    }
    
    public void changeLocAddress(MarklinLocomotive l)
    {
        if (this.model.isAutonomyRunning())
        {
            JOptionPane.showMessageDialog(this, "Cannot edit locomotives while autonomy is running.");
            return;
        }
        
        try
        {
            if (l != null)
            {
                LocomotiveAddressChange edit = new LocomotiveAddressChange(l);
                int result = JOptionPane.showConfirmDialog(
                    this, edit, "Change name/address for " + l.getName(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );
                
                String newAddress = edit.getAddress();
                decoderType newDecoderType = edit.getDecoderType();
                Integer proposedAddress;
                
                if (result == JOptionPane.OK_OPTION && newAddress != null && !"".equals(newAddress.trim()))
                {           
                    if (newDecoderType == decoderType.MFX && newAddress.contains("0x"))
                    {
                        proposedAddress = Integer.valueOf(newAddress.replace("0x", ""), 16);
                    }
                    else
                    {
                        proposedAddress = Integer.valueOf(newAddress);
                    }

                    if (proposedAddress != l.getAddress() || newDecoderType != l.getDecoderType())
                    {
                        this.model.changeLocAddress(l.getName(), proposedAddress, newDecoderType);
                    }
                }
                
                String newName = edit.getLocName();
                
                if (result == JOptionPane.OK_OPTION && newName != null && !l.getName().equals(newName))
                {
                    if (newName.trim().length() == 0)
                    {
                        JOptionPane.showMessageDialog(this,
                            "Please enter a locomotive name");
                        return;
                    }

                    if (newName.length() > MAX_LOC_NAME_DATABASE)
                    {
                        JOptionPane.showMessageDialog(this,
                            "Please enter a locomotive name no longer than " + MAX_LOC_NAME_DATABASE + " characters");
                        return;
                    }

                    if (this.model.getLocByName(newName) != null)
                    {
                        JOptionPane.showMessageDialog(this,
                            "Locomotive " + newName + " already exists in the locomotive DB.  Rename or delete it first.");
                        return;
                    }

                    this.model.renameLoc(l.getName(), newName);
                }
                
                // Refresh everything
                if (result == JOptionPane.OK_OPTION)
                {
                    clearCopyTarget();
                    repaintLoc(true, null);
                    repaintMappings();
                    selector.refreshLocSelectorList();
                    
                    // Update locomotive on graph
                    if (this.model.hasAutoLayout())
                    {
                        this.model.getAutoLayout().sanitizeMultiUnits(l);
                        this.model.getAutoLayout().refreshUI();
                    }
                }
            }
        }
        catch (Exception e)
        {
            this.model.log(e);
            
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
      
    /**
     * Prompts the user to choose locomotives to link to this one
     * @param l 
     */
    public void changeLinkedLocomotives(MarklinLocomotive l)
    {
        // If the multi unit is defined in the central station, enter a read-only mode
        boolean isCSMultiUnit = (l.getDecoderType() == MarklinLocomotive.decoderType.MULTI_UNIT);
        
        if (this.model.isLocLinkedToOthers(l) != null)
        {
            JOptionPane.showMessageDialog(this, "This locomotive is already linked to " + this.model.isLocLinkedToOthers(l).getName() + " and cannot be made a Multi Unit.");
            return;
        }

        if (this.model.isAutonomyRunning())
        {
            JOptionPane.showMessageDialog(this, "Cannot edit Multi Units while autonomy is running.");
            return;
        }

        List<MarklinLocomotive> allLocomotives = !isCSMultiUnit ? this.model.getLocomotives() : l.getCentralStationMultiUnitLocomotives();
        Map<String, Double> currentLinkedLocos = !isCSMultiUnit ? l.getLinkedLocomotiveNames() : l.getCentralStationMultiUnitLocomotiveNames();        
        Map<String, Double> newLinkedLocos = new HashMap<>(currentLinkedLocos);

        // Sort allLocomotives to prioritize currentLinkedLocos and then alphabetically by name
        Collections.sort(allLocomotives, (a, b) ->
        {
            boolean aLinked = currentLinkedLocos.containsKey(a.getName());
            boolean bLinked = currentLinkedLocos.containsKey(b.getName());
            if (aLinked && !bLinked)
            {
                return -1;
            }
            else if (!aLinked && bLinked)
            {
                return 1;
            }
            else
            {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTextField filterField = new JTextField();
        filterField.setToolTipText("Filter by locomotive name");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(filterField, gbc);

        JPanel scrollPanel = new JPanel();
        scrollPanel.setLayout(new GridBagLayout());
        GridBagConstraints scrollGbc = new GridBagConstraints();
        scrollGbc.insets = new Insets(5, 5, 5, 5);
        scrollGbc.fill = GridBagConstraints.HORIZONTAL;

        // Add heading
        JPanel headingPanel = new JPanel(new GridLayout(1, 3));
        JLabel nameLabel = new JLabel("Locomotive Name");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel multiplierLabel = new JLabel("Speed Multiplier (%)");
        multiplierLabel.setToolTipText("Adjust the relative speed of the linked locomotive?");
        multiplierLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel reverseLabel = new JLabel("Reverse");
        reverseLabel.setToolTipText("Swap the direction of the linked locomotive?");
        reverseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headingPanel.add(nameLabel);
        headingPanel.add(multiplierLabel);
        headingPanel.add(reverseLabel);
        scrollGbc.gridy = 0;
        scrollPanel.add(headingPanel, scrollGbc);
        scrollGbc.gridy++;
        scrollPanel.add(Box.createVerticalStrut(10), scrollGbc);

        List<JPanel> rowPanels = new ArrayList<>();
        for (MarklinLocomotive loco : allLocomotives)
        {
            if (
                    isCSMultiUnit || // show all the CS MU's locomotives for informational purposes
                    l.canBeLinkedTo(loco, false) || l.isLinkedTo(loco)) 
            {
                JPanel rowPanel = new JPanel(new GridLayout(1, 3, 10, 5));
                JCheckBox checkBox = new JCheckBox(loco.getName().length() > MAX_MU_SELECTOR_LOC_NAME_LENGTH ?
                        loco.getName().substring(0, MAX_MU_SELECTOR_LOC_NAME_LENGTH) + "..." : 
                        loco.getName(), currentLinkedLocos.containsKey(loco.getName())
                );                 
                checkBox.setToolTipText(loco.getName());
                checkBox.setFocusable(false);
                double multiplierValue = currentLinkedLocos.getOrDefault(loco.getName(), 1.0);
                boolean isReversed = multiplierValue < 0;
                JSlider multiplierSlider = new JSlider(0, 200, (int) (Math.abs(multiplierValue) * 100));
                multiplierSlider.setMajorTickSpacing(50);
                multiplierSlider.setMinorTickSpacing(50);
                multiplierSlider.setPaintTicks(true);
                multiplierSlider.setPaintLabels(true);
                multiplierSlider.setSnapToTicks(false);
                multiplierSlider.setPreferredSize(new Dimension(250, 50));
                multiplierSlider.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                multiplierSlider.setFocusable(true);

                // Custom label table for specific tick labels
                java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
                labelTable.put(0, new JLabel("0"));
                labelTable.put(50, new JLabel("50"));
                labelTable.put(100, new JLabel("100"));
                labelTable.put(150, new JLabel("150"));
                labelTable.put(200, new JLabel("200"));
                multiplierSlider.setLabelTable(labelTable);

                // Update tooltip to show selected value on hover
                multiplierSlider.addMouseMotionListener(new MouseMotionAdapter()
                {
                    @Override
                    public void mouseMoved(MouseEvent e)
                    {
                        JSlider source = (JSlider) e.getSource();
                        source.setToolTipText(String.valueOf(source.getValue()) + "%");
                    }
                });

                // Add change listener to jump to 1 if the value is 0
                multiplierSlider.addChangeListener(e ->
                {
                    if (multiplierSlider.getValue() == 0)
                    {
                        multiplierSlider.setValue(1);
                    }
                });

                // Add mouse listener to reset to 100 on double-click
                multiplierSlider.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (e.getClickCount() == 2)
                        {
                            multiplierSlider.setValue(100);
                        }
                    }
                });

                JCheckBox reverseCheckBox = new JCheckBox("", isReversed);
                reverseCheckBox.setFocusable(false);

                // Central station multi units are read-only
                if (isCSMultiUnit)
                {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(false);
                    reverseCheckBox.setEnabled(false);
                    multiplierSlider.setEnabled(false);
                    filterField.setVisible(false);
                }
                
                rowPanel.add(checkBox);
                rowPanel.add(multiplierSlider);
                rowPanel.add(reverseCheckBox);
                rowPanels.add(rowPanel);

                scrollGbc.gridy++;
                scrollPanel.add(rowPanel, scrollGbc);
                scrollGbc.gridy++;
                scrollPanel.add(Box.createVerticalStrut(10), scrollGbc);
            }
        }

        filterField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                filter();
            }

            private void filter()
            {
                String text = filterField.getText().toLowerCase();
                scrollPanel.removeAll();
                scrollGbc.gridy = 0;
                scrollGbc.gridwidth = 3;
                scrollPanel.add(headingPanel, scrollGbc);
                scrollGbc.gridy++;
                scrollPanel.add(Box.createVerticalStrut(10), scrollGbc);

                boolean hasRows = false;

                for (JPanel rowPanel : rowPanels) {
                    JCheckBox checkBox = (JCheckBox) rowPanel.getComponent(0);
                    if (checkBox.getText().toLowerCase().contains(text)) {
                        scrollGbc.gridy++;
                        scrollPanel.add(rowPanel, scrollGbc);
                        scrollGbc.gridy++;
                        scrollPanel.add(Box.createVerticalStrut(10), scrollGbc);
                        hasRows = true;
                    }
                }

                if (!hasRows)
                {
                    headingPanel.setVisible(false);
                    scrollGbc.gridwidth = 3;
                    scrollPanel.add(new JLabel("No locomotives found."), scrollGbc);
                }
                else
                {
                    headingPanel.setVisible(true);
                }

                scrollPanel.revalidate();
                scrollPanel.repaint();
                filterField.requestFocus();
            }
        });

        JScrollPane scrollPane = new JScrollPane(scrollPanel);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Make scrolling faster
        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollPane.getVerticalScrollBar().getUnitIncrement() * 16);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        panel.add(scrollPane, gbc);

        // Call filter() once to render initial list
        filterField.setText("");
        
        // Ensure the field is focused
        filterField.addAncestorListener(new AncestorListener()
        {
            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}

            @Override
            public void ancestorAdded(AncestorEvent event) {
                event.getComponent().requestFocusInWindow();
            }
        });
        
        // sanity check - should never happen unless CS data is corrupt
        if (isCSMultiUnit && allLocomotives.isEmpty())
        {
            headingPanel.setVisible(false);
            scrollGbc.gridwidth = 3;
            scrollPanel.add(new JLabel("No locomotives found."), scrollGbc);
            filterField.setVisible(false);
        }   

        int result = JOptionPane.showConfirmDialog(null, panel, "Multi Unit: Link locomotives to " + l.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION && !isCSMultiUnit)
        {
            newLinkedLocos.clear();
            Component[] components = scrollPanel.getComponents();

            for (Component component : components)
            {
                if (component instanceof JPanel && !(component.equals(headingPanel)))
                {
                    JPanel rowPanel = (JPanel) component;
                    JCheckBox checkBox = (JCheckBox) rowPanel.getComponent(0);
                    JSlider multiplierSlider = (JSlider) rowPanel.getComponent(1);
                    JCheckBox reverseCheckBox = (JCheckBox) rowPanel.getComponent(2);

                    if (checkBox.isSelected())
                    {
                        double multiplier = multiplierSlider.getValue() / 100.0;
                        if (reverseCheckBox.isSelected())
                        {
                            multiplier = -multiplier;
                        }

                        if (multiplier != 0 && multiplier >= -2 && multiplier <= 2)
                        {
                            newLinkedLocos.put(checkBox.getToolTipText(), multiplier);
                        }
                    }
                }
            }

            l.preSetLinkedLocomotives(newLinkedLocos);
            l.setLinkedLocomotives();
            this.repaintLoc(true, null);

            // Ensure there are no conflicts on the graph
            if (this.model.hasAutoLayout())
            {
                this.model.getAutoLayout().sanitizeMultiUnits(l);
                this.repaintAutoLocListFull();
                this.model.getAutoLayout().refreshUI();
            }
        }
    }

    /**
     * Allows the user to specify notes for the given locomotive
     * @param l
     */
    public void changeLocNotes(Locomotive l)
    {
        if (l != null)
        {
            JTextArea textArea = new JTextArea(l.getNotes());
            textArea.setColumns(70);
            textArea.setRows(20);
            //textArea.setLineWrap(true);
            //textArea.setWrapStyleWord(true);
            textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

            // Ensure the text area is in focus
            textArea.addAncestorListener(new AncestorListener()
            {
                @Override
                public void ancestorRemoved(AncestorEvent event) {}

                @Override
                public void ancestorMoved(AncestorEvent event) {}

                @Override
                public void ancestorAdded(AncestorEvent event) {
                    event.getComponent().requestFocusInWindow();
                }
            });
            
            int confirm = JOptionPane.showConfirmDialog(this, new JScrollPane(textArea), "Enter notes for " + l.getName(), JOptionPane.OK_CANCEL_OPTION);

            if (confirm == JOptionPane.YES_OPTION)
            {
                l.setNotes(textArea.getText().substring(0, Math.min(textArea.getText().length(), MAX_LOC_NOTES_LENGTH)));
            }
        }
    }
    
    public void deleteLoc(String value)
    {
        if (this.model.isAutonomyRunning())
        {
            JOptionPane.showMessageDialog(this, "Cannot edit locomotives while autonomy is running.");
            return;
        }
        
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + value + " from the database?", "Please Confirm", JOptionPane.YES_NO_OPTION))
        {
            Locomotive l = this.model.getLocByName(value);

            if (l != null)
            {
                // Also delete locomotive from active loc list
                if (l.equals(this.activeLoc))
                {
                    this.activeLoc = null;
                }

                List<JButton> keys = new LinkedList(this.currentLocMapping().keySet());
                for (JButton key : keys)
                {
                    if (this.currentLocMapping().get(key) == l)
                    {
                        this.currentLocMapping().put(key, null);
                    }
                }

                this.model.deleteLoc(value);
                clearCopyTarget();
                repaintLoc();
                repaintMappings();
                selector.refreshLocSelectorList();
                
                // Remove locomotive from graph
                if (this.model.hasAutoLayout())
                {
                    this.model.getAutoLayout().locDeleted(l);
                    this.repaintAutoLocListFull();
                    this.model.getAutoLayout().refreshUI();
                }
            }
        }
    }
    
    private void ActiveLocLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ActiveLocLabelMouseReleased
        
        this.showLocSelector();
        
    }//GEN-LAST:event_ActiveLocLabelMouseReleased

    private void decrementLocSpeed(int step)
    {                                                  
        if (this.activeLoc != null)
        {
            setLocSpeed(Math.max(this.activeLoc.getSpeed() - step, 0));
        }
    } 
    
    private void incrementLocSpeed(int step)
    {
        if (this.activeLoc != null)
        {
            setLocSpeed(Math.min(this.activeLoc.getSpeed() + step , 100));
        }
    }
        
    private synchronized void sliderClickedSynced(java.awt.event.MouseEvent evt)
    {
        if (evt.getClickCount() == 2 && SwingUtilities.isRightMouseButton(evt))
        {
            JSlider slider = (JSlider) evt.getSource();

            new Thread(() ->
            {
                JButton b = this.rSliderMapping.get(slider);
                
                Locomotive l = this.currentLocMapping().get(b);

                if (l != null)
                {                    
                    l.setSpeed(0);
                    l.switchDirection();
                    
                    // Change active loc if setting selected
                    if (prefs.getBoolean(SLIDER_SETTING_PREF, false))
                    {
                        this.displayCurrentButtonLoc(b);
                    }
                }

            }).start();
        }
        else
        {
            updateSliderSpeed(evt);
        }
    }
        
    private void SpeedSliderDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SpeedSliderDragged
       setLocSpeed(SpeedSlider.getValue());
    }//GEN-LAST:event_SpeedSliderDragged
    
    /**
     * Validates that a component's value is an integer
     * @param evt 
     * @param hex 
     */
    public static void validateInt(java.awt.event.KeyEvent evt, boolean hex)
    {
        JTextField field = (JTextField) evt.getSource();
        
        try
        {
            int addr = Integer.parseInt(field.getText());
            
            if (addr < 0)
            {
                field.setText(field.getText().replaceAll("[^0-9]", ""));    
            }
        }
        catch (Exception e)
        {            
            if (field.getText() != null)
            {
                String candidate = field.getText().replaceAll("[^0-9" + (hex ? "A-Fa-fx" : "") + "]", "");

                if (!candidate.equals(field.getText())) field.setText(candidate);
            }
        }
    }
    
    /**
     * Validates that a component's value is simple string
     * @param evt 
     */
    public static void validateLayoutName(java.awt.event.KeyEvent evt)
    {
        JTextField field = (JTextField) evt.getSource();
        
        if (field.getText() != null)
        {
            String candidate = field.getText().replaceAll("[^0-9a-zA-Z_\\. \\-]", "");

            if (!candidate.equals(field.getText())) field.setText(candidate);
        }
    }
    
    /**
     * Validates that a component's value is an integer
     * @param evt 
     * @param maxLength 
     */
    public static void limitLength(java.awt.event.KeyEvent evt, int maxLength)
    {
        JTextField field = (JTextField) evt.getSource();
        
        // Trim if needed
        if (field.getText() != null && field.getText().length() > maxLength)
        {
            field.setText(field.getText().substring(0, maxLength));
        }
    }
         
    public void editRoute(String routeName)
    {
        new Thread(()->
        {  
            if (routeName != null && this.model.getRoute(routeName) != null)
            {          
                if (routeEditor != null && routeEditor.isVisible())
                {
                    JOptionPane.showMessageDialog(this, "You can only edit one route at a time.  Close the editor window first.");
                    routeEditor.requestFocus();
                }
                else
                {
                    MarklinRoute currentRoute = this.model.getRoute(routeName);
                    
                    routeEditor = new RouteEditor("Edit Route: " + routeName + " (ID: " + currentRoute.getId() + ")",
                            this, routeName, currentRoute.toCSV(), currentRoute.isEnabled(), currentRoute.getS88(), currentRoute.getTriggerType(),
                        currentRoute.getConditionCSV(), currentRoute.isLocked());
                }
            }
        }).start();
    }
    
    public MarklinRoute getRouteAtCursor(MouseEvent evt)
    {
        try
        {
            return (MarklinRoute) this.RouteList.getValueAt(this.RouteList.rowAtPoint(evt.getPoint()), this.RouteList.columnAtPoint(evt.getPoint()));
        }
        catch (Exception e)
        {
            return null;
        }  
    }
    
    public int getTimetableIndexAtCursor(MouseEvent evt)
    {
        try
        {
            return this.timetable.rowAtPoint(evt.getPoint());
        }
        catch (Exception e)
        {
            return -1;
        }  
    }
    
    public Object getTimetableEntryAtCursor(MouseEvent evt)
    {
        try
        {
            return this.model.getAutoLayout().getTimetable().get(
                this.timetable.rowAtPoint(evt.getPoint())
            );
        }
        catch (Exception e)
        {
            return null;
        }  
    }
    
    public void duplicateRoute(String routeName)
    {
        new Thread(()->
        {  
            if (routeName != null)
            {
                MarklinRoute currentRoute = this.model.getRoute(routeName);

                if (currentRoute != null && currentRoute instanceof MarklinRoute)
                {
                    String proposedName = currentRoute.getName() + " (Copy %s)";

                    int i = 1;

                    while (this.model.getRoute(String.format(proposedName, i)) != null)
                    {
                        i++;
                    }

                    this.model.newRoute(String.format(proposedName, i), currentRoute.getRoute(), 
                            currentRoute.getS88(), currentRoute.getTriggerType(), false, currentRoute.getConditions()); 

                    // Ensure route changes are synced
                    this.model.syncWithCS2();
                    this.repaintLayout();   
                    
                    refreshRouteList();
                }  
            }
        }).start();
    }
        
    private void BulkEnableOrDisable(boolean enable)
    {
        new Thread(()->
        { 
            String searchString = JOptionPane.showInputDialog(this, "Enter search string; matching routes with S88 will be " + (enable ? "enabled" : "disabled") +". * matches all.", "*");

            if (!"".equals(searchString))
            {
                for (String routeName : this.model.getRouteList())
                {
                    MarklinRoute r = this.model.getRoute(routeName);

                    if (r.hasS88() || r.isEnabled())
                    {
                        if (r.getName().contains(searchString) || "*".equals(searchString))
                        {
                             this.model.editRoute(r.getName(), r.getName(), r.getRoute(),
                                        r.getS88(), r.getTriggerType(), enable, r.getConditions());
                        }
                    }
                }

                // Ensure route changes are synced
                this.model.syncWithCS2();
                this.repaintLayout();
                
                refreshRouteList();
            }
        }).start();
    }
    
    public void enableOrDisableRoute(String routeName, boolean enable)
    {  
        new Thread(() -> 
        {
            MarklinRoute r = this.model.getRoute(routeName);

            if (r.hasS88())
            {
                this.model.editRoute(r.getName(), r.getName(), r.getRoute(), r.getS88(), r.getTriggerType(), enable, 
                        r.getConditions());

                // Ensure route changes are synced
                this.model.syncWithCS2();
                this.repaintLayout();
                
                refreshRouteList();
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Route must have an S88 configured to fire automatically.");
            }
        }).start();
    }
        
    public void resetFocus()
    {
        this.KeyboardTab.requestFocus();
    }
    
    private void LocFunctionsPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LocFunctionsPanelMouseEntered
        this.KeyboardTab.requestFocus();
    }//GEN-LAST:event_LocFunctionsPanelMouseEntered

    /**
     * Checks if the current OS is Windows
     * @return 
     */
    private boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }
    
    /**
     * Unzips an empty layout to the current folder and returns the path
     * @return
     */
    private String initializeEmptyLayout(String folderName)
    {
        try
        {
            String from = RESOURCE_PATH + DEMO_LAYOUT_ZIP;
            File to = new File(DEMO_LAYOUT_ZIP);
            
            copyResource(from, to);
            
            this.model.log("Attempting to extract " + to.getAbsolutePath());
            
            File outputFolder = new File(folderName);
            this.unzipFile(Paths.get(to.getPath()), outputFolder.getAbsolutePath());
            to.delete();
                        
            return outputFolder.getAbsolutePath();
        } 
        catch (IOException ex) 
        {
            this.model.log("Error during demo layout extraction.");
            
            this.model.log(ex);
        }
        
        return null;
    }
    
    /**
     * Copies a JAR resource file to the specified path
     * @param resource
     * @param output
     * @throws IOException 
     */
    private void copyResource(String resource, File output) throws IOException
    {
        InputStream is = TrainControlUI.class.getResource(resource).openStream();
        OutputStream os = new FileOutputStream(output.getPath());

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1)
        {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }
    
    /**
     * Unzips a file
     * https://howtodoinjava.com/java/io/unzip-file-with-subdirectories/
     * @param filePathToUnzip
     * @throws java.io.IOException
     */
    private void unzipFile(Path filePathToUnzip, String folderName)
    {
        Path parentDir = filePathToUnzip.getParent();
        String fileName = filePathToUnzip.toFile().getName();
        Path targetDir = Paths.get(folderName);

        //Open the file
        try (ZipFile zip = new ZipFile(filePathToUnzip.toFile()))
        {
            FileSystem fileSystem = FileSystems.getDefault();
            Enumeration<? extends ZipEntry> entries = zip.entries();

            //We will unzip files in this folder
            if (!targetDir.toFile().isDirectory() && !targetDir.toFile().mkdirs()) 
            {
              throw new IOException("failed to create directory " + targetDir);
            }

            //Iterate over entries
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();

                File f = new File(targetDir.resolve(Paths.get(entry.getName())).toString());

                // If directory then create a new directory in uncompressed folder
                if (entry.isDirectory())
                {
                    if (!f.isDirectory() && !f.mkdirs())
                    {
                      throw new IOException("failed to create directory " + f);
                    }
                }

                // Else create the file
                else
                {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                      throw new IOException("failed to create directory " + parent);
                    }

                    try(InputStream in = zip.getInputStream(entry))
                    {
                      Files.copy(in, f.toPath());
                    }
                    catch (Exception e)
                    {
                      this.model.log(e);
                    }
                }
            }
        } 
        catch (IOException e)
        {
            this.model.log(e);
        }
    }
    
    /**
     * Restores layout popups
     */
    public void restoreLayoutTitles()
    {
        try
        {
            if (prefs.getBoolean(REMEMBER_WINDOW_LOCATION, false))
            {
                // Handle different layout sizes
                for (Integer size : layoutSizes.values())
                {
                    String preferenceKey = LAYOUT_TITLES_PREF + "_" + size;
                    String titles = prefs.get(preferenceKey, "");

                    if (!titles.isEmpty())
                    {
                        List<String> layoutTitles = Arrays.asList(titles.split("<<delimiter>>"));

                        for (String layoutTitle : layoutTitles)
                        {
                            if (this.model.getLayoutList().contains(layoutTitle))
                            {
                                this.showLayoutPopup(layoutTitle, size);                      
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            this.model.log("Error loading window locations.");
            
            if (this.model.isDebug())
            {
                this.model.log(e);
            }
        }
    }

    /**
     * Saves location of layout popups
     */
    private void saveLayoutTitles()
    {
        try
        {
            if (prefs.getBoolean(REMEMBER_WINDOW_LOCATION, false))
            {
                Map<String, Set<String>> layoutTitlesMap = new HashMap<>();
                ListIterator<LayoutPopupUI> iter = this.popups.listIterator();

                while (iter.hasNext())
                {
                    LayoutPopupUI currentPopup = iter.next();
                    if (currentPopup.isVisible())
                    {
                        String layoutTitle = currentPopup.getLayoutTitle();
                        String layoutSize = Integer.toString(currentPopup.getLayoutSize()); 

                        if (!layoutTitlesMap.containsKey(layoutSize))
                        {
                            layoutTitlesMap.put(layoutSize, new HashSet<>());
                        }

                        layoutTitlesMap.get(layoutSize).add(layoutTitle);
                    }
                }

                // Reset by default
                for (Integer size : layoutSizes.values())
                {
                    String preferenceKey = LAYOUT_TITLES_PREF + "_" + size;
                    prefs.put(preferenceKey, "");
                }
                  
                for (Map.Entry<String, Set<String>> entry : layoutTitlesMap.entrySet())
                {
                    String layoutSize = entry.getKey();
                    String titles = String.join("<<delimiter>>", entry.getValue());

                    String preferenceKey = LAYOUT_TITLES_PREF + "_" + layoutSize;
                    prefs.put(preferenceKey, titles);
                }
            }
        }
        catch (Exception e)
        {
            this.model.log("Error saving window locations.");
            
            if (this.model.isDebug())
            {
                this.model.log(e);
            }
        }
    }
    
    /**
     * Updates the list of popups, and optionally refreshes the diagrams
     */
    private void updatePopups(boolean doRefresh)
    {
        ListIterator<LayoutPopupUI> iter = this.popups.listIterator();
        while(iter.hasNext())
        {
            LayoutPopupUI currentPopup = iter.next();
            
            if (currentPopup.isVisible())
            {
                if (doRefresh)
                {
                    currentPopup.refreshDiagram();
                }
            }
            else
            {
                iter.remove();
            }
        }
        
        if (this.model.isDebug())
        {
            this.model.log("Currently active popups: " + this.popups.size());
        }
    }
    
    private void EditFunction(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EditFunction

        if(SwingUtilities.isRightMouseButton(evt) && this.activeLoc != null)
        {
            javax.swing.JToggleButton b =
            (javax.swing.JToggleButton) evt.getSource();
            
            if (b.isEnabled())
            {
                Integer fNumber = this.functionMapping.get(b);

                LocomotiveFunctionAssign edit = new LocomotiveFunctionAssign(this.activeLoc, this, fNumber, true);
                int result = JOptionPane.showConfirmDialog(this, edit, "Edit " + this.activeLoc.getName() + " Functions", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                edit.focusImages();
                
                if (result == JOptionPane.OK_OPTION)
                {
                    edit.doApply();
                }
            }
        }
    }//GEN-LAST:event_EditFunction

    private void locIconMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_locIconMouseReleased
        if(SwingUtilities.isRightMouseButton(evt) && this.activeLoc != null)
        {
            this.setLocIcon(this.activeLoc);
        }
    }//GEN-LAST:event_locIconMouseReleased

    public void clearTimetable()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            if (this.getModel().getAutoLayout().isRunning())
            {
                JOptionPane.showMessageDialog(this, "Please wait for all active locomotives to stop.");
                return;
            }
            
            int dialogResult = JOptionPane.showConfirmDialog(
                this, "This will remove all timetable entries. Continue?"
                    , "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if(dialogResult == JOptionPane.NO_OPTION) return;

            this.model.getAutoLayout().setTimetable(new LinkedList<>());
            this.repaintTimetable();
            this.repaintAutoLocListLite();
        }));
    }
    
    public boolean isShowStationLengthsSelected()
    {
        return this.showStationLengths.isSelected();
    }
    
    private void syncFullLocStateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncFullLocStateMenuItemActionPerformed
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            int dialogResult = JOptionPane.showConfirmDialog(this, "This function will query the Central Station for the current function status and direction of all locomotives, and may take several minutes. Continue?", "Sync State", JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION)
            {
                this.syncMenuItem.setEnabled(false);
                this.functionsMenu.setEnabled(false);

                this.model.allFunctionsOff();
                
                new Thread(() ->
                {
                    for (String s : this.model.getLocList())
                    {
                        this.model.syncLocomotive(s);
                    }
                    
                    this.syncMenuItem.setEnabled(true);
                    this.functionsMenu.setEnabled(true);

                }).start();
            }
        }));
    }//GEN-LAST:event_syncFullLocStateMenuItemActionPerformed

    private void syncMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncMenuItemActionPerformed
        doSync(this);
    }//GEN-LAST:event_syncMenuItemActionPerformed

    private void viewDatabaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDatabaseMenuItemActionPerformed
        ActiveLocLabelMouseReleased(null);
    }//GEN-LAST:event_viewDatabaseMenuItemActionPerformed

    private void turnOnLightsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_turnOnLightsMenuItemActionPerformed
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            this.syncMenuItem.setEnabled(false);
            this.functionsMenu.setEnabled(false);

            new Thread(() ->
            {
                List<String> locs = new ArrayList<>();

                for (Map<JButton, Locomotive> m : this.locMapping)
                {
                    for (Locomotive l : m.values())
                    {
                        locs.add(l.getName());
                    }
                }

                this.model.lightsOn(locs);
                this.syncMenuItem.setEnabled(true);
                this.functionsMenu.setEnabled(true);

            }).start();
        }));
    }//GEN-LAST:event_turnOnLightsMenuItemActionPerformed

    private void turnOffFunctionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_turnOffFunctionsMenuItemActionPerformed
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            this.syncMenuItem.setEnabled(false);
            this.functionsMenu.setEnabled(false);
            
            new Thread(() ->
            {
                this.model.allFunctionsOff();
                this.syncMenuItem.setEnabled(true);
                this.functionsMenu.setEnabled(true);
            }).start();
        }));
    }//GEN-LAST:event_turnOffFunctionsMenuItemActionPerformed

    private void backupDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupDataMenuItemActionPerformed
        new Thread(() ->
        {
            this.backupDataMenuItem.setEnabled(false);
            this.saveState(true);
            this.model.saveState(true);
            this.backupDataMenuItem.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Backup complete: check log for file path.");
            
            // Advance to last tab (log)
            this.KeyboardTab.setSelectedIndex(this.KeyboardTab.getComponentCount() - 1);
        }).start();
    }//GEN-LAST:event_backupDataMenuItemActionPerformed

    private void addLocomotiveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocomotiveMenuItemActionPerformed
        this.getLocAdder().setVisible(true);
    }//GEN-LAST:event_addLocomotiveMenuItemActionPerformed

    private void openCS3AppMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCS3AppMenuItemActionPerformed
        new Thread(()->
        { 
            String url = this.model.getCS3AppUrl();         
            Util.openUrl(url);
        }).start();
    }//GEN-LAST:event_openCS3AppMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        
        JOptionPane.showMessageDialog(this, new About(), "About", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void switchCSLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchCSLayoutMenuItemActionPerformed
        // Hide the tab in case loading fails but the model still has the local diagram
        this.switchCSLayoutMenuItem.setEnabled(false);

        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            try
            {
                //this.KeyboardTab.remove(this.layoutPanel);
                this.KeyboardTab.setEnabledAt(1, false);
                if (this.KeyboardTab.getSelectedIndex() == 1)
                {
                    this.KeyboardTab.setSelectedIndex(0);
                }
                
                prefs.put(LAYOUT_OVERRIDE_PATH_PREF, "");
                this.model.clearLayouts();
                this.model.syncWithCS2();
                
                // Set the updated list of layout pages
                initializeTrackDiagram(true);
            }
            catch (Exception e)
            {
                this.model.log("Error synchronizing layout from CS2.");
            }
            
            this.switchCSLayoutMenuItem.setEnabled(true);
            this.repaintPathLabel();
            
        }));
    }//GEN-LAST:event_switchCSLayoutMenuItemActionPerformed

    private void initializeLocalLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initializeLocalLayoutMenuItemActionPerformed
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            JOptionPane.showMessageDialog(this, "In the next window, please select a folder for the new layout.");

            JFileChooser fc = new JFileChooser(prefs.get(LAYOUT_OVERRIDE_PATH_PREF, new File(".").getAbsolutePath()));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int i = fc.showOpenDialog(this);
            if (i == JFileChooser.APPROVE_OPTION)
            {
                File f = fc.getSelectedFile();
                String filepath = f.getPath();

                prefs.put(LAYOUT_OVERRIDE_PATH_PREF, filepath);
                
                this.initializeLocalLayoutMenuItem.setEnabled(false);

                this.createAndApplyEmptyLayout(filepath, false);
            }
        }));
    }//GEN-LAST:event_initializeLocalLayoutMenuItemActionPerformed

    private void chooseLocalDataFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseLocalDataFolderMenuItemActionPerformed
        new Thread(()->
        { 
            JFileChooser fc = new JFileChooser(prefs.get(LAYOUT_OVERRIDE_PATH_PREF, new File(".").getAbsolutePath()));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int i = fc.showOpenDialog(this);
            if (i == JFileChooser.APPROVE_OPTION)
            {
                File f = fc.getSelectedFile();
                String filepath = f.getPath();

                prefs.put(LAYOUT_OVERRIDE_PATH_PREF, filepath);
            }
            else
            {
                return;
            }

            this.model.syncWithCS2();
            
            if (!this.model.getLayoutList().isEmpty() && this.isLocalLayout())
            {   
                this.initializeTrackDiagram(true);
            }
            else
            {
                this.repaintLoc();
                this.repaintLayout();
                JOptionPane.showMessageDialog(this, "Invalid path or corrupt data. Ensure this folder is the parent of the CS2's \"config\" layout folder hierarchy.");   
            }    
        }).start();
    }//GEN-LAST:event_chooseLocalDataFolderMenuItemActionPerformed

    private void showCurrentLayoutFolderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCurrentLayoutFolderMenuItemActionPerformed
        
        String info = this.getLayoutPath(false);
        
        if (!info.contains("None"))
        {
            int result = JOptionPane.showConfirmDialog(this, info + "\n\nDo you want to view the files?", "Layout Source Info", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION)
            {
                this.getLayoutPath(true);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, info); 
        }
    }//GEN-LAST:event_showCurrentLayoutFolderMenuItemActionPerformed

    private void mainMenuBarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mainMenuBarKeyPressed
        LocControlPanelKeyPressed(evt);
    }//GEN-LAST:event_mainMenuBarKeyPressed

    private void fileMenuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileMenuKeyPressed
        LocControlPanelKeyPressed(evt);
    }//GEN-LAST:event_fileMenuKeyPressed

    private void locomotiveMenuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_locomotiveMenuKeyPressed
        LocControlPanelKeyPressed(evt);
    }//GEN-LAST:event_locomotiveMenuKeyPressed

    private void functionsMenuKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_functionsMenuKeyReleased
        LocControlPanelKeyPressed(evt);
    }//GEN-LAST:event_functionsMenuKeyReleased

    private void layoutMenuKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_layoutMenuKeyPressed
        LocControlPanelKeyPressed(evt);
    }//GEN-LAST:event_layoutMenuKeyPressed

    private void KeyboardTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_KeyboardTabStateChanged
        if (KeyboardTab.getSelectedIndex() == KeyboardTab.getTabCount() - 2 && this.model != null && this.stats != null)
        {
            this.stats.refresh();
        }
    }//GEN-LAST:event_KeyboardTabStateChanged

    private void locCommandPanelsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_locCommandPanelsMouseClicked
        this.KeyboardTab.requestFocus();
    }//GEN-LAST:event_locCommandPanelsMouseClicked

    private void showStationLengthsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showStationLengthsMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            this.ensureGraphUIVisible();
            this.updateVisiblePoints();
            prefs.putBoolean(SHOW_STATION_LENGTH, this.showStationLengths.isSelected());
        }
        else
        {
            this.showStationLengths.setSelected(!this.showStationLengths.isSelected());
        }
    }//GEN-LAST:event_showStationLengthsMouseReleased

    private void hideInactiveMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hideInactiveMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            this.ensureGraphUIVisible();
            this.updateVisiblePoints();
            prefs.putBoolean(HIDE_INACTIVE_PREF, this.hideInactive.isSelected());
        }
        else
        {
            this.hideInactive.setSelected(!this.hideInactive.isSelected());
        }
    }//GEN-LAST:event_hideInactiveMouseReleased

    private void hideReversingMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hideReversingMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            this.ensureGraphUIVisible();
            this.updateVisiblePoints();
            prefs.putBoolean(HIDE_REVERSING_PREF, this.hideReversing.isSelected());
        }
        else
        {
            this.hideReversing.setSelected(!this.hideReversing.isSelected());
        }
    }//GEN-LAST:event_hideReversingMouseReleased

    private void turnOnFunctionsOnDepartureMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_turnOnFunctionsOnDepartureMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setTurnOnFunctionsOnDeparture(this.turnOnFunctionsOnDeparture.isSelected());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_turnOnFunctionsOnDepartureMouseReleased

    private void simulateMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_simulateMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setSimulate(this.simulate.isSelected());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_simulateMouseReleased

    private void turnOffFunctionsOnArrivalMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_turnOffFunctionsOnArrivalMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setTurnOffFunctionsOnArrival(this.turnOffFunctionsOnArrival.isSelected());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_turnOffFunctionsOnArrivalMouseReleased

    private void atomicRoutesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_atomicRoutesMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setAtomicRoutes(this.atomicRoutes.isSelected());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_atomicRoutesMouseReleased

    private void preArrivalSpeedReductionMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_preArrivalSpeedReductionMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setPreArrivalSpeedReduction(Double.valueOf(this.preArrivalSpeedReduction.getValue()) / 100);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_preArrivalSpeedReductionMouseReleased

    private void defaultLocSpeedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultLocSpeedMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setDefaultLocSpeed(this.defaultLocSpeed.getValue());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_defaultLocSpeedMouseReleased

    private void maxDelayMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxDelayMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setMaxDelay(this.maxDelay.getValue());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_maxDelayMouseReleased

    private void maxLocInactiveSecondsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxLocInactiveSecondsMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setMaxLocInactiveSeconds(this.maxLocInactiveSeconds.getValue() * 60);
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_maxLocInactiveSecondsMouseReleased

    private void minDelayMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minDelayMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setMinDelay(this.minDelay.getValue());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_minDelayMouseReleased

    private void minDelayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minDelayStateChanged

    }//GEN-LAST:event_minDelayStateChanged

    private void timetableCaptureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timetableCaptureActionPerformed

        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            if (this.getModel().getAutoLayout().isRunning())
            {
                JOptionPane.showMessageDialog(this, "Please wait for all active locomotives to stop.");
            }
            else
            {
                this.model.getAutoLayout().setTimetableCapture(!this.model.getAutoLayout().isTimetableCapture());
            }

            this.timetableCapture.setSelected(this.model.getAutoLayout().isTimetableCapture());
        }));
    }//GEN-LAST:event_timetableCaptureActionPerformed

    private void executeTimetableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeTimetableActionPerformed

        this.executeTimetable.setEnabled(false);

        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            if (!this.getModel().getPowerState())
            {
                JOptionPane.showMessageDialog(this, "To start autonomy, please turn the track power on, or cycle the power.");
                this.executeTimetable.setEnabled(true);
                return;
            }

            if (this.getModel().getAutoLayout().isRunning())
            {
                JOptionPane.showMessageDialog(this, "Please wait for all active locomotives to stop.");
                this.executeTimetable.setEnabled(true);
                return;
            }

            if (this.model.getAutoLayout().getTimetable().isEmpty())
            {
                JOptionPane.showMessageDialog(this, "There are no timetable entries. Capture some commands first.");
                this.executeTimetable.setEnabled(true);
                return;
            }

            // Conditional route warning
            for (String routeName : this.model.getRouteList())
            {
                MarklinRoute r = this.model.getRoute(routeName);

                if (r.isEnabled())
                {
                    this.model.log("Autonomy warning: active conditional route " + r.getName() + " may lead to unpredictable behavior");

                    if (!conditionalRouteWarningShown)
                    {
                        int dialogResult = JOptionPane.showConfirmDialog(this,
                            "One or more conditional routes are active, which may cause unpredictable behavior. Proceed?", "Confirm", JOptionPane.YES_NO_OPTION);

                        if (dialogResult == JOptionPane.NO_OPTION)
                        {
                            return;
                        }
                        else
                        {
                            conditionalRouteWarningShown = true;
                            break;
                        }
                    }
                }
            }

            // Validate starting locations
            List<Locomotive> seen = new ArrayList<>();

            for (int i = this.model.getAutoLayout().getUnfinishedTimetablePathIndex(); i < this.model.getAutoLayout().getTimetable().size(); i++)
            {
                TimetablePath ttp = this.model.getAutoLayout().getTimetable().get(i);

                if (!seen.contains(ttp.getLoc()))
                {
                    Point locLocation = this.model.getAutoLayout().getLocomotiveLocation(ttp.getLoc());
                    if (locLocation == null || !locLocation.equals(ttp.getStart()))
                    {
                        JOptionPane.showMessageDialog(this, "Locomotive " + ttp.getLoc().getName() + " must be moved to " + ttp.getStart());
                        this.executeTimetable.setEnabled(true);
                        return;
                    }

                    seen.add(ttp.getLoc());
                }
            }

            // Disable capture if it was enabled
            this.model.getAutoLayout().setTimetableCapture(false);
            this.timetableCapture.setSelected(this.model.getAutoLayout().isTimetableCapture());

            new Thread(() ->
            {
                this.startAutonomy.setEnabled(false);
                this.model.getAutoLayout().executeTimetable();
                this.executeTimetable.setEnabled(true);
                this.startAutonomy.setEnabled(true);
                this.exportJSON.setEnabled(true);
            }).start();

            this.gracefulStop.setEnabled(true);
        }));
    }//GEN-LAST:event_executeTimetableActionPerformed

    private void NextKeyboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextKeyboardActionPerformed
        this.switchKeyboard(this.keyboardNumber + 1);
    }//GEN-LAST:event_NextKeyboardActionPerformed

    private void PrevKeyboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevKeyboardActionPerformed
        this.switchKeyboard(this.keyboardNumber - 1);
    }//GEN-LAST:event_PrevKeyboardActionPerformed

    private void UpdateSwitchState(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateSwitchState
        javax.swing.JToggleButton b = (javax.swing.JToggleButton) evt.getSource();
        int switchId = Integer.parseInt(b.getText());

        if (b.isSelected())
        {
            b.setBackground(COLOR_SWITCH_RED);
        }
        else
        {
            b.setBackground(COLOR_SWITCH_GREEN);
        }

        // Underline when red
        Font font = b.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, b.isSelected() ? TextAttribute.UNDERLINE_ON : -1);
        b.setFont(font.deriveFont(attributes));

        new Thread(() ->
            {
                this.model.setAccessoryState(switchId, getKeyboardProtocol(), b.isSelected());
            }).start();
    }//GEN-LAST:event_UpdateSwitchState

    private void BulkDisableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BulkDisableActionPerformed

        new Thread(()->
            {
                BulkEnableOrDisable(false);
            }).start();
    }//GEN-LAST:event_BulkDisableActionPerformed

    private void BulkEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BulkEnableActionPerformed

        new Thread(()->
            {
                BulkEnableOrDisable(true);
            }).start();
    }//GEN-LAST:event_BulkEnableActionPerformed

    private void sortByIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByIDActionPerformed
        new Thread(()->
            {
                prefs.putBoolean(ROUTE_SORT_PREF, false);
                this.refreshRouteList();
            }).start();
    }//GEN-LAST:event_sortByIDActionPerformed

    private void sortByNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByNameActionPerformed
        new Thread(()->
            {
                prefs.putBoolean(ROUTE_SORT_PREF, true);
                this.refreshRouteList();
            }).start();
    }//GEN-LAST:event_sortByNameActionPerformed

    private void AddRouteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddRouteButtonActionPerformed

        new Thread(()->
        {
            String proposedName = "Route %s";
            int i = 1;

            while (this.model.getRoute(String.format(proposedName, i)) != null)
            {
                i++;
            }

            if (routeEditor != null && routeEditor.isVisible())
            {
                JOptionPane.showMessageDialog(this, "You can only edit one route at a time.");
                routeEditor.requestFocus();
                routeEditor.toFront();
            }
            else
            {
                routeEditor = new RouteEditor("Add New Route", this, String.format(proposedName, i), "", false, 0, MarklinRoute.s88Triggers.CLEAR_THEN_OCCUPIED, "", false);
            }
        }).start();
    }//GEN-LAST:event_AddRouteButtonActionPerformed

    private void RouteListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RouteListMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt))
        {
            //Object route = this.RouteList.getValueAt(this.RouteList.getSelectedRow(), this.RouteList.getSelectedColumn());
            MarklinRoute route = this.getRouteAtCursor(evt);

            if (route != null && route instanceof MarklinRoute)
            {
                // We need to set this in case there are popup windows
                this.setAlwaysOnTop(true);
                
                int dialogResult = JOptionPane.showConfirmDialog(RoutePanel, "Execute route " + route.getName() + "? (ID: " + this.model.getRouteId(route.getName()) + ")", "Route Execution", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION)
                {
                    new Thread(() ->
                    {
                        executeRoute(route.getName());
                    }).start();
                }
                
                // Revert preference
                windowAlwaysOnTopMenuItemActionPerformed(null);
            }
        }
    }//GEN-LAST:event_RouteListMouseClicked

    /**
     * Housekeeping to refresh UI after layout has been edited
     */
    public void layoutEditingComplete()
    {
        this.editLayoutButton.setEnabled(false);

        this.model.syncWithCS2();
        
        // Store previously selected page
        int oldIndex = this.LayoutList.getSelectedIndex();

        // Update list of pages
        if (this.model.getLayoutList() != null && !this.model.getLayoutList().isEmpty())
        {
            this.LayoutList.setModel(new DefaultComboBoxModel(this.model.getLayoutList().toArray()));
        }

        // Restore index
        if (this.LayoutList.getModel().getSize() > oldIndex)
        {
            this.LayoutList.setSelectedIndex(oldIndex);
        }

        this.repaintLayout();

        this.updatePopups(true);
        
        this.editLayoutButton.setEnabled(true);
        
        // Revert preference
        windowAlwaysOnTopMenuItemActionPerformed(null);
        
        // Revert preference for graph UI
        if (this.graphViewer != null) this.graphViewer.setAlwaysOnTop(this.isAlwaysOnTop());
    }
    
    /**
     * Opens the layout editor app for the current layout
     * @param evt 
     */
    private void editLayoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLayoutButtonActionPerformed
        
        if (!this.isLocalLayout())
        {
            JOptionPane.showMessageDialog(this, "Editing is only supported for local layout files.\n\n"
                + "Edit your layout via the Central Station, or see the Layouts menu to download this layout or initialize a new one.");
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {    
            // New native editor
            LayoutEditor popup = new LayoutEditor(
                this.model.getLayout(this.LayoutList.getSelectedItem().toString()),
                this.layoutSizes.get(this.SizeList.getSelectedItem().toString()),
                this,
                this.LayoutList.getSelectedIndex()
            );

            // Force window to not be on top
            this.setAlwaysOnTop(false);
            
            if (this.graphViewer != null) this.graphViewer.setAlwaysOnTop(false);

            popup.render();
        }));
    }//GEN-LAST:event_editLayoutButtonActionPerformed

    private void showLayoutPopup(String layoutName, int size)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            LayoutPopupUI popup = new LayoutPopupUI(
                this.model.getLayout(layoutName),
                size,
                this,
                this.LayoutList.getSelectedIndex()
            );

            popup.render();
            popups.add(popup);
            updatePopups(false);
            repaintLoc();
        }));
    }
    
    private void allButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allButtonActionPerformed

        int size = this.LayoutList.getItemCount();
        for (int i = 0; i < size; i++)
        {
            String layoutName = LayoutList.getItemAt(i).toString();

            showLayoutPopup(layoutName, this.layoutSizes.get(this.SizeList.getSelectedItem().toString()));
        }
    }//GEN-LAST:event_allButtonActionPerformed

    private void smallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_smallButtonActionPerformed

        showLayoutPopup(this.LayoutList.getSelectedItem().toString(), this.layoutSizes.get("Small"));
    }//GEN-LAST:event_smallButtonActionPerformed

    private void layoutNewWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutNewWindowActionPerformed

        showLayoutPopup(this.LayoutList.getSelectedItem().toString(), this.layoutSizes.get("Large"));
    }//GEN-LAST:event_layoutNewWindowActionPerformed

    private void SizeListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SizeListActionPerformed
        repaintLayoutFromCache();
    }//GEN-LAST:event_SizeListActionPerformed

    private void LayoutListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LayoutListActionPerformed
        repaintLayoutFromCache();
    }//GEN-LAST:event_LayoutListActionPerformed

    private void FiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FiveButtonActionPerformed
        setLocSpeed(44);
    }//GEN-LAST:event_FiveButtonActionPerformed

    private void SixButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SixButtonActionPerformed
        setLocSpeed(55);
    }//GEN-LAST:event_SixButtonActionPerformed

    private void OneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OneButtonActionPerformed
        setLocSpeed(0);
    }//GEN-LAST:event_OneButtonActionPerformed

    private void TwoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TwoButtonActionPerformed
        setLocSpeed(11);
    }//GEN-LAST:event_TwoButtonActionPerformed

    private void ThreeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThreeButtonActionPerformed
        setLocSpeed(22);
    }//GEN-LAST:event_ThreeButtonActionPerformed

    private void FourButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FourButtonActionPerformed
        setLocSpeed(33);
    }//GEN-LAST:event_FourButtonActionPerformed

    private void SevenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SevenButtonActionPerformed
        setLocSpeed(66);
    }//GEN-LAST:event_SevenButtonActionPerformed

    private void NineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NineButtonActionPerformed
        setLocSpeed(88);
    }//GEN-LAST:event_NineButtonActionPerformed

    private void EightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EightButtonActionPerformed
        setLocSpeed(77);
    }//GEN-LAST:event_EightButtonActionPerformed

    private void ZeroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZeroButtonActionPerformed
        setLocSpeed(100);
    }//GEN-LAST:event_ZeroButtonActionPerformed

    private void AltEmergencyStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AltEmergencyStopActionPerformed
        new Thread(() ->
            {
                this.model.stopAllLocs();
            }).start();
    }//GEN-LAST:event_AltEmergencyStopActionPerformed

    private void ShiftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShiftButtonActionPerformed
        setLocSpeed(0);
    }//GEN-LAST:event_ShiftButtonActionPerformed

    private void SpacebarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SpacebarButtonActionPerformed
        stopLoc();
    }//GEN-LAST:event_SpacebarButtonActionPerformed

    private void LeftArrowLetterButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LeftArrowLetterButtonPressed
        switchDirection();
    }//GEN-LAST:event_LeftArrowLetterButtonPressed

    private void RightArrowLetterButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RightArrowLetterButtonPressed
        switchDirection();
    }//GEN-LAST:event_RightArrowLetterButtonPressed

    private void DownArrowLetterButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownArrowLetterButtonPressed
        decrementLocSpeed(SPEED_STEP);
    }//GEN-LAST:event_DownArrowLetterButtonPressed

    private void UpArrowLetterButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpArrowLetterButtonPressed
        incrementLocSpeed(SPEED_STEP);
    }//GEN-LAST:event_UpArrowLetterButtonPressed

    private void updateSliderSpeed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateSliderSpeed
        JSlider slider = (JSlider) evt.getSource();

        new Thread(() ->
            {
                JButton b = this.rSliderMapping.get(slider);

                Locomotive l = this.currentLocMapping().get(b);

                if (l != null)
                {
                    if (l.getSpeed() != slider.getValue())
                    {
                        l.setSpeed(slider.getValue());
                    }

                    // Change active loc if setting selected
                    if (prefs.getBoolean(SLIDER_SETTING_PREF, false))
                    {
                        this.displayCurrentButtonLoc(b);
                    }
                }
            }).start();
    }//GEN-LAST:event_updateSliderSpeed

    private void sliderClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderClicked

        sliderClickedSynced(evt);
    }//GEN-LAST:event_sliderClicked

    private void NextLocMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextLocMappingActionPerformed
        this.switchLocMapping(this.locMappingNumber + 1);
    }//GEN-LAST:event_NextLocMappingActionPerformed

    private void PrevLocMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevLocMappingActionPerformed
        this.switchLocMapping(this.locMappingNumber - 1);
    }//GEN-LAST:event_PrevLocMappingActionPerformed

    private void LetterButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LetterButtonPressed
        this.displayCurrentButtonLoc((javax.swing.JButton) evt.getSource(), true);
    }//GEN-LAST:event_LetterButtonPressed

    private void toggleMenuBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleMenuBarActionPerformed
        prefs.putBoolean(MENUBAR_SETTING_PREF, this.toggleMenuBar.isSelected());
        displayMenuBar();
    }//GEN-LAST:event_toggleMenuBarActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        WindowClosed(null);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void keyboardQwertyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyboardQwertyMenuItemActionPerformed
        if (this.getSelectedKeyboardType() >= 0)
        {
            prefs.put(TrainControlUI.KEYBOARD_LAYOUT, Integer.toString(this.getSelectedKeyboardType()));
            this.applyKeyboardType(TrainControlUI.KEYBOARD_TYPES[this.getSelectedKeyboardType()]);

            if (this.model != null)
            {
                this.model.log("Updated keyboard type to: " + TrainControlUI.KEYBOARD_TYPES[this.getSelectedKeyboardType()]);
                this.repaintLoc(true, null);
            }
        }
    }//GEN-LAST:event_keyboardQwertyMenuItemActionPerformed

    private void slidersChangeActiveLocMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slidersChangeActiveLocMenuItemActionPerformed
        prefs.putBoolean(SLIDER_SETTING_PREF, this.slidersChangeActiveLocMenuItem.isSelected());
    }//GEN-LAST:event_slidersChangeActiveLocMenuItemActionPerformed

    private void windowAlwaysOnTopMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windowAlwaysOnTopMenuItemActionPerformed
        prefs.putBoolean(ONTOP_SETTING_PREF, this.windowAlwaysOnTopMenuItem.isSelected());
        setAlwaysOnTop(prefs.getBoolean(ONTOP_SETTING_PREF, ONTOP_SETTING_DEFAULT));
    }//GEN-LAST:event_windowAlwaysOnTopMenuItemActionPerformed

    /**
     * Jumps to the locomotive tab on the autonomy page
     */
    public void jumpToAutonomyLocTab()
    {
        this.KeyboardTab.setSelectedIndex(2);
        
        // Advance to locomotive autonomy tab
        this.locCommandPanels.setSelectedIndex(
            1
        );

        this.KeyboardTab.requestFocus();
    }
    
    /**
     * Jumps to the layout tab
     */
    public void jumpToLayoutTab()
    {
        int currentIndex = this.KeyboardTab.getSelectedIndex();
        
        this.KeyboardTab.setSelectedIndex(1);
        
        if (currentIndex != 1)
        {
            this.KeyboardTab.requestFocus();
        }
    }
    
    private void validateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validateButtonActionPerformed

        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            resetLayoutStationLabels();
            
            // If valid, confirm before we overwrite
            if (this.model.hasAutoLayout() && this.model.getAutoLayout().isValid()
                && !this.model.getAutoLayout().getPoints().isEmpty())
            {
                try
                {
                    if (!this.model.getAutoLayout().toJSON().equals(this.autonomyJSON.getText()))
                    {
                        int dialogResult = JOptionPane.showConfirmDialog(
                            this, "Autonomy graph state has changed.  Reloading the JSON configuration will reset any unsaved changes.  Proceed?"
                            , "Confirm Reset", JOptionPane.YES_NO_OPTION);

                        if (dialogResult == JOptionPane.NO_OPTION)
                        {
                            // Reopen the window
                            this.ensureGraphUIVisible();
                            return;
                        }
                        else
                        {
                            // Hide the window
                            if (this.graphViewer != null) this.graphViewer.setVisible(false);
                        }
                    }
                }
                catch (Exception e)
                {
                    this.model.log(e);
                }
            }

            // Offer to load a blank graph if there is no JSON
            if (this.autonomyJSON.getText().trim().equals(""))
            {
                this.loadDefaultBlankGraphActionPerformed(null);
            }

            this.model.parseAuto(this.autonomyJSON.getText());

            if (!this.model.hasAutoLayout() || !this.model.getAutoLayout().isValid())
            {
                locCommandPanels.remove(this.locCommandTab);
                locCommandPanels.remove(this.timetablePanel);
                locCommandPanels.remove(this.autoSettingsPanel);

                this.startAutonomy.setEnabled(false);

                JOptionPane.showMessageDialog(this, "JSON validation failed.  Check log for details.\n\n" + Layout.getLastError());

                this.KeyboardTab.requestFocus();

                this.exportJSON.setEnabled(false);
            }
            else
            {
                locCommandPanels.addTab("Locomotive Commands", this.locCommandTab);
                locCommandPanels.addTab("Timetable", this.timetablePanel);
                locCommandPanels.addTab("Autonomy Settings", this.autoSettingsPanel);
                loadAutoLayoutSettings();

                this.startAutonomy.setEnabled(true);
                this.executeTimetable.setEnabled(true);

                // Advance to locomotive tab
                this.locCommandPanels.setSelectedIndex(
                    1
                    //(this.locCommandPanels.getSelectedIndex() + 1)
                    //% this.locCommandPanels.getComponentCount()
                );

                this.KeyboardTab.requestFocus();

                this.renderAutoLayoutGraph();

                this.graphViewer.requestFocus();

                this.exportJSON.setEnabled(true);
                this.gracefulStop.setEnabled(false);
                
                if (evt != null && evt instanceof CustomActionEvent)
                {
                    this.KeyboardTab.setSelectedIndex(0);
                    this.requestFocus();
                    this.toFront();
                }
            }

            // Stop all locomotives
            AltEmergencyStopActionPerformed(null);

        }));
    }//GEN-LAST:event_validateButtonActionPerformed

    private void gracefulStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gracefulStopActionPerformed

        this.gracefulStop.setEnabled(false);
        this.startAutonomy.setEnabled(true);

        new Thread(() ->
            {
                this.getModel().getAutoLayout().stopLocomotives();

                // Ensure list is updated after stopping a timetable run
                this.repaintAutoLocListLite();
            }).start();
    }//GEN-LAST:event_gracefulStopActionPerformed

    /**
     * Called externally
     * @throws Exception 
     */
    public void requestStartAutonomy() throws Exception
    {
        if (this.startAutonomy.isEnabled())
        {
            startAutonomyActionPerformed(null);
        }
        else
        {
            throw new Exception("Unable to start autonomy.  Wait for all trains to reach their stations.");
        }
    }
    
    /**
     * Called externally
     * @throws Exception 
     */
    public void requestStopAutonomy() throws Exception
    {
        if (this.gracefulStop.isEnabled())
        {
            gracefulStopActionPerformed(null);
        }
        else
        {
            throw new Exception("Stop request already issued. Wait for all trains to reach their stations.");
        }
    }
    
    /**
     * Opens the autonomy UI
     */
    public void ensureGraphUIVisible()
    {
        // Show graph window if it was closed
        if (this.graphViewer != null && !this.graphViewer.isVisible())
        {
            this.graphViewer.setVisible(true);
            this.repaintLoc();
            updateVisiblePoints();
        }
    }
    
    private void startAutonomyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAutonomyActionPerformed

        new Thread(() ->
            {
                if (!this.model.getPowerState())
                {
                    JOptionPane.showMessageDialog(this, "To start autonomy, please turn the track power on, or cycle the power.");
                    return;
                }

                this.ensureGraphUIVisible();

                for (String routeName : this.model.getRouteList())
                {
                    MarklinRoute r = this.model.getRoute(routeName);

                    if (r.isEnabled())
                    {
                        this.model.log("Autonomy warning: active conditional route " + r.getName() + " may lead to unpredictable behavior");

                        if (!conditionalRouteWarningShown)
                        {
                            int dialogResult = JOptionPane.showConfirmDialog(this,
                                "One or more conditional routes are active, which may cause unpredictable behavior. Proceed?", "Confirm", JOptionPane.YES_NO_OPTION);

                            if (dialogResult == JOptionPane.NO_OPTION)
                            {
                                return;
                            }
                            else
                            {
                                conditionalRouteWarningShown = true;
                                break;
                            }
                        }
                    }
                }

                if (this.model.getAutoLayout().getLocomotivesToRun().isEmpty())
                {
                    JOptionPane.showMessageDialog(this, "Please add some locomotives to the graph.");
                    return;
                }

                if (this.model.getAutoLayout().isValid() && !this.model.getAutoLayout().isRunning())
                {
                    new Thread( () ->
                    {
                        this.model.getAutoLayout().runLocomotives();
                    }).start();

                    this.startAutonomy.setEnabled(false);
                    this.gracefulStop.setEnabled(true);
                }
                else if (this.model.getAutoLayout().isRunning())
                {
                    JOptionPane.showMessageDialog(this, "Please wait for active locomotives to stop.");
                }
                else if (!this.model.getAutoLayout().isValid())
                {
                    JOptionPane.showMessageDialog(this, "Layout state is no longer valid due to new data from Central Station.  Please re-validate JSON.");
                }
            }).start();
    }//GEN-LAST:event_startAutonomyActionPerformed

    private void loadDefaultBlankGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDefaultBlankGraphActionPerformed
        
        String[] options = {"Blank Graph", "Sample Graph", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Do you want to create a new autonomy graph?  This will overwrite any existing configuration. Right-click the graph window to add points and edges, and to place locomotives.  See the documentation for details.",
             "Graph Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice)
        {
            case 0: // Blank
            case 1: // Sample
                
                try
                {
                    this.autonomyJSON.setText(
                       new BufferedReader(new InputStreamReader(TrainControlUI.class.getResource(RESOURCE_PATH + (choice == 1 ? AUTONOMY_SAMPLE : AUTONOMY_BLANK)).openStream())).lines().collect(Collectors.joining("\n"))
                    );

                    if (evt != null) this.validateButtonActionPerformed(null);
                }
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(this, "Error opening the graph file.");
                    this.model.log(e);
                }
                break;
            case 2: // Cancel
                break;
            default:
                break;
        }
    }//GEN-LAST:event_loadDefaultBlankGraphActionPerformed

    private void jsonDocumentationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jsonDocumentationButtonActionPerformed
        Util.openUrl(README_URL);
    }//GEN-LAST:event_jsonDocumentationButtonActionPerformed

    private void autosaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autosaveActionPerformed
        prefs.putBoolean(AUTOSAVE_SETTING_PREF, this.autosave.isSelected());
    }//GEN-LAST:event_autosaveActionPerformed

    private void loadJSONButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadJSONButtonActionPerformed
        this.loadJSONButton.setEnabled(false);
        new Thread(()->
            {
                try
                {
                    JFileChooser fc = getJSONFileChooser(JFileChooser.OPEN_DIALOG);
                    int i = fc.showOpenDialog(this);

                    if (i == JFileChooser.APPROVE_OPTION)
                    {
                        File f = fc.getSelectedFile();

                        this.autonomyJSON.setText(new String(Files.readAllBytes(Paths.get(f.getPath()))));
                        prefs.put(LAST_USED_FOLDER, f.getParent());

                        validateButtonActionPerformed(null);
                    }
                }
                catch (HeadlessException | IOException e)
                {
                    JOptionPane.showMessageDialog(this, "Error opening file.");

                    this.model.log(e);
                }

                this.loadJSONButton.setEnabled(true);
            }).start();
    }//GEN-LAST:event_loadJSONButtonActionPerformed

    private void exportJSONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportJSONActionPerformed

        new Thread(() ->
            {
                try
                {
                    JOptionPane.showMessageDialog(this, new AutoJSONExport(this.getModel().getAutoLayout().toJSON(), this, "autonomy"),
                        "Export current graph state to JSON file", JOptionPane.PLAIN_MESSAGE
                    );

                    // Place in clipboard
                    StringSelection selection = new StringSelection(this.model.getAutoLayout().toJSON());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
                catch (Exception e)
                {
                    this.model.log(e);

                    this.model.log("JSON error: " + e.getMessage());

                    JOptionPane.showMessageDialog(this, "Failed to generate/export JSON.  Check log for details.");
                }
            }).start();
    }//GEN-LAST:event_exportJSONActionPerformed

    private void showKeyboardHintsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showKeyboardHintsMenuItemActionPerformed
        prefs.putBoolean(SHOW_KEYBOARD_HINTS_PREF, this.showKeyboardHintsMenuItem.isSelected());
        displayKeyboardHints(prefs.getBoolean(SHOW_KEYBOARD_HINTS_PREF, true));
    }//GEN-LAST:event_showKeyboardHintsMenuItemActionPerformed

    private void activeLocInTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeLocInTitleActionPerformed
        prefs.putBoolean(ACTIVE_LOC_IN_TITLE, this.activeLocInTitle.isSelected());
        this.repaintLoc();
    }//GEN-LAST:event_activeLocInTitleActionPerformed

    private void exportRoutesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportRoutesMenuItemActionPerformed
        new Thread(() ->
            {
                try
                {
                    JOptionPane.showMessageDialog(this, new AutoJSONExport(this.getModel().exportRoutes(), this, "routes"),
                        "Export route data", JOptionPane.PLAIN_MESSAGE
                    );

                    // Place in clipboard
                    StringSelection selection = new StringSelection(this.model.exportRoutes());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
                catch (Exception e)
                {
                    this.model.log("JSON error: " + e.getMessage());
                    this.model.log(e);

                    JOptionPane.showMessageDialog(this, "Failed to generate/export route JSON.  Check log for details.");
                }
            }).start();
    }//GEN-LAST:event_exportRoutesMenuItemActionPerformed

    private void importRoutesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importRoutesMenuItemActionPerformed
        this.importRoutesMenuItem.setEnabled(false);

        new Thread(() ->
            {
                try
                {
                    JFileChooser fc = getJSONFileChooser(JFileChooser.OPEN_DIALOG);
                    int i = fc.showOpenDialog(this);

                    if (i == JFileChooser.APPROVE_OPTION)
                    {
                        File f = fc.getSelectedFile();

                        this.model.importRoutes(new String(Files.readAllBytes(Paths.get(f.getPath()))));

                        prefs.put(LAST_USED_FOLDER, f.getParent());

                        // Ensure route changes are synced
                        this.model.syncWithCS2();
                        this.repaintLayout();
                        this.repaintLoc();
                        refreshRouteList();
                    }
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(this, "Failed to import routes.  Check log for details.");

                    this.model.log("Route import error: " + e.getMessage());

                    this.model.log(e);
                }

                this.importRoutesMenuItem.setEnabled(true);

            }).start();
    }//GEN-LAST:event_importRoutesMenuItemActionPerformed

    private void changeIPMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeIPMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, "IP preference (was " + TrainControlUI.getPrefs().get(TrainControlUI.IP_PREF, null) + ") has been reset.  Restart TrainControl, then specify a new IP.");
        TrainControlUI.getPrefs().remove(TrainControlUI.IP_PREF);
    }//GEN-LAST:event_changeIPMenuItemActionPerformed

    private void quickFindMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickFindMenuItemActionPerformed
        if (!this.model.getLocomotives().isEmpty())
        {
            quickLocSearch(); 
        }
        else
        {
            JOptionPane.showMessageDialog(this, "There are no locomotives in the database.");
        }
    }//GEN-LAST:event_quickFindMenuItemActionPerformed

    private void viewReleasesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewReleasesMenuItemActionPerformed
        Util.openUrl(UPDATE_URL);
    }//GEN-LAST:event_viewReleasesMenuItemActionPerformed

    private void checkForUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkForUpdatesActionPerformed
        prefs.putBoolean(CHECK_FOR_UPDATES, this.checkForUpdates.isSelected());
    }//GEN-LAST:event_checkForUpdatesActionPerformed

    private void downloadUpdateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadUpdateMenuItemActionPerformed
        
        new Thread(() ->
        {
            try
            {
                File f = new File("TrainControl_v" + LATEST_VERSION.replace(".", "_") + ".jar");

                if (!f.exists())
                {
                    Util.downloadFile(TrainControlUI.LATEST_DOWNLOAD_URL, f);
                }
                
                javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
                {
                    JOptionPane.showMessageDialog(this, "Downloaded update to:\n" + f.getAbsolutePath() + "\n\nReplace your existing .jar with the new file.");
                    
                    showFileExplorer(new File(f.getAbsoluteFile().toString()).getParentFile());
                }));
            }
            catch (Exception e)
            {
                javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
                {
                    JOptionPane.showMessageDialog(this, "Error downloading the update file.");
                }));
                
                this.model.log("Error downloading the update file.");

                if (this.model.isDebug())
                {
                    this.model.log(e);
                }
            }
        }).start();
    }//GEN-LAST:event_downloadUpdateMenuItemActionPerformed

    private void powerOffStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerOffStartupActionPerformed
        if (((JRadioButtonMenuItem) evt.getSource()).isSelected())  
        {
            prefs.putInt(AUTO_POWER_ON, 1);
        }
    }//GEN-LAST:event_powerOffStartupActionPerformed

    private void powerOnStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerOnStartupActionPerformed
        if (((JRadioButtonMenuItem) evt.getSource()).isSelected())  
        {
            prefs.putInt(AUTO_POWER_ON, 0);
        }
    }//GEN-LAST:event_powerOnStartupActionPerformed

    private void powerNoChangeStartupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerNoChangeStartupActionPerformed
        if (((JRadioButtonMenuItem) evt.getSource()).isSelected())  
        {
            prefs.putInt(AUTO_POWER_ON, 2);
        }
    }//GEN-LAST:event_powerNoChangeStartupActionPerformed

    private void rememberLocationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rememberLocationMenuItemActionPerformed
        prefs.putBoolean(REMEMBER_WINDOW_LOCATION, this.rememberLocationMenuItem.isSelected());
        
        // If we don't do this, the preference will get deleted
        if (this.popups.isEmpty() && this.rememberLocationMenuItem.isSelected())
        {
            this.restoreLayoutTitles();
        }
    }//GEN-LAST:event_rememberLocationMenuItemActionPerformed
    
    // Track the last hovered row and column
    private int lastRow = -1;
    private int lastColumn = -1;
    
    private void RouteListMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RouteListMouseMoved
        int row = RouteList.rowAtPoint(evt.getPoint());
        int column = RouteList.columnAtPoint(evt.getPoint());

        if (row != lastRow || column != lastColumn)
        {
            if (lastRow != -1 && lastColumn != -1)
            {
                CustomTableRenderer lastRenderer = (CustomTableRenderer) RouteList.getColumnModel().getColumn(lastColumn).getCellRenderer();
                lastRenderer.resetHoveredCell();
                lastRow = -1;
                lastColumn = -1;
            }

            CustomTableRenderer renderer = (CustomTableRenderer) RouteList.getColumnModel().getColumn(column).getCellRenderer();
            renderer.setHoveredCell(row, column);
            lastRow = row;
            lastColumn = column;
            RouteList.repaint();
        }
    }//GEN-LAST:event_RouteListMouseMoved

    private void RouteListMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RouteListMouseExited
        
        for (int column = 0; column < RouteList.getColumnCount(); column++)
        {
            CustomTableRenderer renderer = (CustomTableRenderer) RouteList.getColumnModel().getColumn(column).getCellRenderer();
            renderer.resetHoveredCell();
        }
        
        lastRow = -1;
        lastColumn = -1;
        RouteList.repaint();
    }//GEN-LAST:event_RouteListMouseExited

    private void maximumLatencyMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maximumLatencyMouseReleased
        
        if (!this.isAutoLayoutRunning())
        {            
            try
            {
                this.model.getAutoLayout().setMaxLatency(this.maximumLatency.getValue());
                this.maximumLatency.setValue(this.model.getAutoLayout().getMaxLatency());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_maximumLatencyMouseReleased

    private void AutoLoadAutonomyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AutoLoadAutonomyMenuItemActionPerformed
        prefs.putBoolean(AUTO_LOAD_AUTONOMY, this.AutoLoadAutonomyMenuItem.isSelected());
    }//GEN-LAST:event_AutoLoadAutonomyMenuItemActionPerformed

    private void maxActiveTrainsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxActiveTrainsMouseReleased
        if (!this.isAutoLayoutRunning())
        {
            try
            {
                this.model.getAutoLayout().setMaxActiveTrains(this.maxActiveTrains.getValue());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
                loadAutoLayoutSettings();
            }
        }
    }//GEN-LAST:event_maxActiveTrainsMouseReleased

    /**
     * Opens a window and prompts the user for layout name
     * @param defaultVal
     * @return 
     */
    private String promptUserForLayout(String defaultVal)
    {
        JTextField textField = new JTextField()
        {
            @Override
            public void addNotify()
            {
                super.addNotify();
                javax.swing.Timer focusTimer = new javax.swing.Timer(50, e -> requestFocusInWindow());
                focusTimer.setRepeats(false);
                focusTimer.start();
            }
        };
        
        textField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent evt)
            {
                TrainControlUI.validateLayoutName(evt); 
                TrainControlUI.limitLength(evt, 40);
            }
        });

        textField.setText(defaultVal);
        textField.requestFocusInWindow();
        
        // Display the input dialog
        int result = JOptionPane.showConfirmDialog(
            this,
            textField,
            "Enter new layout page name:",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION && textField.getText() != null && textField.getText().trim().length() > 0)
        {
            return textField.getText().trim();
        }
        
        return null;
    }
    
    private void renameLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameLayoutMenuItemActionPerformed

        this.KeyboardTab.setSelectedIndex(1);

        String name = promptUserForLayout(this.LayoutList.getSelectedItem().toString());

        if (name != null)
        {
            duplicateOrRenameCurrentLayout(name, true, false, false);
        }
    }//GEN-LAST:event_renameLayoutMenuItemActionPerformed

    private void duplicateLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateLayoutMenuItemActionPerformed
        
        String newName = this.LayoutList.getSelectedItem().toString() + " copy";

        // Check if the new name already exists in the layout list
        while (this.model.getLayoutList().contains(newName))
        {
            newName += " copy";
        }

        this.KeyboardTab.setSelectedIndex(1);

        duplicateOrRenameCurrentLayout(newName, false, true, false);
    }//GEN-LAST:event_duplicateLayoutMenuItemActionPerformed

    /**
     * Duplicates or renames the current layout page, optionally clearing it
     * @param newLayoutName - name of the new layout
     * @param rename - true if renaming
     * @param duplicate - true if duplicating
     * @param blank - true to clear
     */
    private void duplicateOrRenameCurrentLayout(String newLayoutName, boolean rename, boolean duplicate, boolean blank)
    {
        if (!this.isLocalLayout())
        {
            JOptionPane.showMessageDialog(this, "Cannot manipulate remote layouts.");
            return;
        }
        
        if (this.model.getLayoutList().contains(newLayoutName))
        {
            JOptionPane.showMessageDialog(this, "A page called " + newLayoutName + " already exists. Delete or rename it first.");
            return;
        }
                
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            try
            {
                List<String> layoutList = this.model.getLayoutList();
                String currentLayout = this.LayoutList.getSelectedItem().toString();

                if (rename)
                {
                    layoutList.remove(currentLayout);
                }
                
                if (blank)
                {
                    this.model.getLayout(currentLayout).clear();
                }
                
                this.model.getLayout(currentLayout).saveChanges(
                    newLayoutName, duplicate
                );
    
                layoutList.add(newLayoutName);
                
                MarklinLayout.writeLayoutIndex(this.getLocalLayoutPath(), layoutList);

                this.layoutEditingComplete();
                
                this.LayoutList.setSelectedItem(newLayoutName);
                this.repaintLayout();   
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(this, "Error saving layout: " + ex.getMessage());
                this.model.log(ex);
            }
        }));
    }
    
    private void deleteLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLayoutMenuItemActionPerformed
        
        if (!this.isLocalLayout())
        {
            JOptionPane.showMessageDialog(this, "Cannot manipulate remote layouts.");
            return;
        }
        
        this.KeyboardTab.setSelectedIndex(1);
        
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            if (this.model.getLayoutList().size() <= 1)
            {
                JOptionPane.showMessageDialog(this, "Cannot delete the only remaining page.");
                return;
            }
            
            int dialogResult = JOptionPane.showConfirmDialog(
                this, "This will delete layout page \"" + this.LayoutList.getSelectedItem().toString() + "\". Are you sure?"
                , "Confirm Deletion", JOptionPane.YES_NO_OPTION
            );

            if (dialogResult == JOptionPane.YES_OPTION)
            {
                try
                {
                    List<String> layoutList = this.model.getLayoutList();

                    this.model.getLayout(this.LayoutList.getSelectedItem().toString()).deleteLayoutFile();

                    layoutList.remove(this.LayoutList.getSelectedItem().toString());
                    MarklinLayout.writeLayoutIndex(this.getLocalLayoutPath(), layoutList);

                    this.layoutEditingComplete();
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(this, "Error saving layout: " + ex.getMessage());
                    this.model.log(ex);
                }
            }
        }));
    }//GEN-LAST:event_deleteLayoutMenuItemActionPerformed

    private void addBlankPageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBlankPageMenuItemActionPerformed

        this.KeyboardTab.setSelectedIndex(1);

        String name = promptUserForLayout("");

        if (name != null)
        {
            duplicateOrRenameCurrentLayout(name, false, true, true);
        }
    }//GEN-LAST:event_addBlankPageMenuItemActionPerformed

    /**
     * Old way to edit track diagrams
     */
    private void openLegacyTrackDiagramEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLegacyTrackDiagramEditorActionPerformed
        
        if (!this.isLocalLayout())
        {
            JOptionPane.showMessageDialog(this, "Editing is only supported for local layout files.\n\n"
                + "Edit your layout via the Central Station, or see the Layouts menu to initialize a local track diagram.");
            return;
        }
        
        if (!this.isWindows())
        {
            JOptionPane.showMessageDialog(this, "Layout editing is currently only supported on Windows.");
            return;
        }

        this.KeyboardTab.setSelectedIndex(1);
        
        this.openLegacyTrackDiagramEditor.setEnabled(false);

        // Force window to not be on top
        this.setAlwaysOnTop(false);
        
        if (this.graphViewer != null) this.graphViewer.setAlwaysOnTop(false);

        new Thread(() ->
            {
                try
                {
                    String layoutUrl = this.model.getLayout(this.LayoutList.getSelectedItem().toString()).getUrl().replaceAll(" ", "%20");
                    Path p = Paths.get(new URL(layoutUrl).toURI());

                    File app = new File(DIAGRAM_EDITOR_EXECUTABLE);

                    // Extract the binary
                    if (!app.exists())
                    {
                        File zippedApp = new File(DIAGRAM_EDITOR_EXECUTABLE_ZIP);

                        this.model.log("Unpacking track diagram editor executable...");

                        copyResource(RESOURCE_PATH + DIAGRAM_EDITOR_EXECUTABLE_ZIP, zippedApp);

                        this.model.log("Attempting to extract " + zippedApp.getAbsolutePath());

                        this.unzipFile(Paths.get(zippedApp.getPath()), (new File("")).getAbsolutePath());
                        zippedApp.delete();
                    }

                    // Delete the binary on exit
                    app.deleteOnExit();

                    // Execute the app
                    String cmd = app.getPath() + " edit \"" + p.toString() + "\"";

                    this.model.log("Running layout editor: " + cmd);

                    Runtime rt = Runtime.getRuntime();
                    Process pr = rt.exec(cmd);

                    pr.waitFor();

                    this.model.log("Editing session complete.");

                    this.layoutEditingComplete();
                }
                catch (Exception ex)
                {
                    this.model.log("Layout editing error: " + ex.getMessage());

                    this.model.log(ex);
                }

                this.openLegacyTrackDiagramEditor.setEnabled(true);

                // Revert preference
                windowAlwaysOnTopMenuItemActionPerformed(null);
                
                if (this.graphViewer != null) this.graphViewer.setAlwaysOnTop(this.isAlwaysOnTop());

            }).start();
    }//GEN-LAST:event_openLegacyTrackDiagramEditorActionPerformed

    private void editCurrentPageActionPerformedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCurrentPageActionPerformedActionPerformed
        
        this.KeyboardTab.setSelectedIndex(1);

        this.editLayoutButtonActionPerformed(null);
    }//GEN-LAST:event_editCurrentPageActionPerformedActionPerformed

    private void MM2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MM2ActionPerformed
        repaintSwitches();
        prefs.putBoolean(PREFERRED_KEYBOARD_MM2, ((JRadioButton) evt.getSource()).isSelected());
    }//GEN-LAST:event_MM2ActionPerformed

    private void DCCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DCCActionPerformed
        repaintSwitches();
        prefs.putBoolean(PREFERRED_KEYBOARD_MM2, !((JRadioButton) evt.getSource()).isSelected());
    }//GEN-LAST:event_DCCActionPerformed

    private void downloadCSLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadCSLayoutMenuItemActionPerformed

        if (!isLocalLayout() && !this.model.getLayoutList().isEmpty())
        {
            javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
            {
                JOptionPane.showMessageDialog(this, "In the next window, choose the folder where you want to download the Central Station layout.  TrainControl will then switch to it as the local data source.");

                try
                {
                    // Prompt the user to choose a folder
                    JFileChooser fc = new JFileChooser(
                        prefs.get(LAST_USED_FOLDER, new File(".").getAbsolutePath())
                    );

                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    int i = fc.showOpenDialog(this);

                    if (i == JFileChooser.APPROVE_OPTION)
                    {
                        File path = fc.getSelectedFile();

                        this.model.downloadLayout(path);

                        // Load the layout
                        prefs.put(LAYOUT_OVERRIDE_PATH_PREF, path.getAbsolutePath());
                        this.model.syncWithCS2();
                        this.repaintLayout();
                        this.KeyboardTab.setSelectedIndex(1);

                        JOptionPane.showMessageDialog(this, "Download complete.  Layout saved in:\n" + path.getAbsolutePath());
                    }
                }
                catch (Exception e)
                {
                    this.model.log("Error downloading Central Station layout.");
                    this.model.log(e);
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }));
        } 
        else
        {
            JOptionPane.showMessageDialog(this, "No Central Station layout is currently available.");
        }
    }//GEN-LAST:event_downloadCSLayoutMenuItemActionPerformed

    /**
     * Returns whether addresses should be shown in track diagrams, and ensures the setting is propagated to all layouts
     * @return 
     */
    private boolean getShowLayoutAddresses()
    {
        return this.menuItemShowLayoutAddresses.isSelected();
    }
    
    /**
     * Tells each layout to display address labels
     */
    private void ensureShowLayoutPreference()
    {
        for (String layoutName : this.model.getLayoutList())
        {
            this.model.getLayout(layoutName).setShowAddress(this.menuItemShowLayoutAddresses.isSelected());
        }
    }
    
    private void menuItemShowLayoutAddressesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemShowLayoutAddressesActionPerformed
        prefs.putBoolean(LAYOUT_SHOW_ADDRESSES, this.menuItemShowLayoutAddresses.isSelected());
                
        if (!this.model.getLayoutList().isEmpty())
        {      
            this.ensureShowLayoutPreference();
            
            // Repaint diagrams
            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
            {
                this.repaintLayout(false, false);
                this.updatePopups(true);
            }));
        }
    }//GEN-LAST:event_menuItemShowLayoutAddressesActionPerformed

    public final void displayKeyboardHints(boolean visibility)
    {
        this.PrimaryControls.setVisible(visibility);
        this.controlsPanel.setVisible(visibility);
    }
    
    private Accessory.accessoryDecoderType getKeyboardProtocol()
    {
        if (this.DCC.isSelected())
        {
            return MarklinAccessory.stringToAccessoryDecoderType(this.DCC.getText());
        }
        
        return MarklinAccessory.stringToAccessoryDecoderType(this.MM2.getText());
    }
    
    public void deleteTimetableEntry(MouseEvent evt)
    {
        try
        {
            if (this.getModel().getAutoLayout().isRunning())
            {
                JOptionPane.showMessageDialog(this, "Please wait for all active locomotives to stop.");
                return;
            }
            
            int index = this.getTimetableIndexAtCursor(evt);
            
            if (index >= 0)
            {
                int dialogResult = JOptionPane.showConfirmDialog(
                    this, "This will remove entry #"+ index + " (" +this.getTimetableEntryAtCursor(evt)+"). Continue?"
                    , "Confirm Deletion", JOptionPane.YES_NO_OPTION
                );

                if (dialogResult == JOptionPane.NO_OPTION) return;
         
                this.model.getAutoLayout().getTimetable().remove(index);
                this.repaintTimetable();
            }
        }
        catch (Exception e)
        {
            this.model.log(e);

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }    
    }
    
    public void updateTimetableDelay(MouseEvent evt)
    {
        try
        {
            if (this.getModel().getAutoLayout().isRunning())
            {
                JOptionPane.showMessageDialog(this, "Please wait for all active locomotives to stop.");
                return;
            }
            
            TimetablePath ttp = (TimetablePath) getTimetableEntryAtCursor(evt);

            if (ttp != null)
            {
                String input = JOptionPane.showInputDialog(this, "Enter delay (seconds) before this route executes: ", (int) (ttp.getSecondsToNext() / 1000.0));
                
                if (input != null)
                {
                    ttp.setSecondsToNext(Long.parseLong(input) * 1000);
                    this.repaintTimetable();
                } 
            }
        }
        catch (Exception e)
        {
            this.model.log(e);

            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Restarts timetable execution from the top
     */
    public void restartTimetable()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            if (this.getModel().getAutoLayout().isRunning())
            {
                JOptionPane.showMessageDialog(this, "Please wait for all active locomotives to stop.");
                return;
            }
            
            // Check if a reset will do anything
            if (this.getModel().getAutoLayout().getUnfinishedTimetablePathIndex() == 0
                    && !this.getModel().getAutoLayout().getTimetable().isEmpty()
                    && this.getModel().getAutoLayout().getTimetable().get(0).getExecutionTime() == 0
            )
            {
                JOptionPane.showMessageDialog(this, "The timetable is already reset.");
                return;
            }

            int dialogResult = JOptionPane.showConfirmDialog(
                this, "This will mark all timetable entries as unvisited to allow the next execution to start from the top. Continue?"
                    , "Confirm Restart", JOptionPane.YES_NO_OPTION);

            if(dialogResult == JOptionPane.NO_OPTION) return;

            this.getModel().getAutoLayout().resetTimetable();
            this.repaintTimetable();
        }));
    }
        
    public void setFunctionIcon(Locomotive l, JButton source)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {    
            LocomotiveFunctionAssign edit = new LocomotiveFunctionAssign(l, this, 0, false);
            // Select the locomotive
            source.doClick();

            JOptionPane.showMessageDialog(this, edit, "Edit " + l.getName() + " Functions", JOptionPane.PLAIN_MESSAGE);
            edit.focusFno();
        }));
    }
    
    public void clearLocIcon(Locomotive l)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            l.setLocalImageURL(null);
            this.model.syncWithCS2();
            this.repaintLoc(true, null);
            this.repaintMappings(Collections.singletonList(l), true);
            this.selector.refreshLocSelectorList();
        }));
    }
    
    /**
     * Allows a local image to be set for a locomotive
     * @param l 
     */
    public void setLocIcon(Locomotive l)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() -> 
        {
            String currentPath = null;

            if (l.getLocalImageURL() != null)
            {
                File currentIcon = new File(l.getLocalImageURL());

                if (currentIcon.exists())
                {
                    currentPath = currentIcon.getParent();
                }

                this.model.log("Current icon for " + l.getName() + " is " + l.getLocalImageURL());
            }
            
            JFileChooser fc = new JFileChooser(
                currentPath != null ? currentPath : prefs.get(LAST_USED_ICON_FOLDER, new File(".").getAbsolutePath())
            );

            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileFilter filter = new FileNameExtensionFilter("JPEG, PNG, GIF, BMP images", "jpg", "png", "gif", "jpeg", "jpe", "bmp");
            fc.setFileFilter(filter);

            fc.setAcceptAllFileFilterUsed(false);

            int i = fc.showOpenDialog(this);

            if (i == JFileChooser.APPROVE_OPTION)
            {
                File f = fc.getSelectedFile();

                l.setLocalImageURL(Paths.get(f.getAbsolutePath()).toUri().toString());
                prefs.put(LAST_USED_ICON_FOLDER, f.getParent());

                this.repaintLoc(true, null);
                this.repaintMappings(Collections.singletonList(l), true);
                this.selector.refreshLocSelectorList();
                // TODO - clear icon setting if load failed
            } 
        }));
    }
    
    /**
     * Returns a file chooser for autonomy files
     * @param type
     * @return 
     */
    public JFileChooser getJSONFileChooser(int type)
    {
        JFileChooser fc = new JFileChooser(
            prefs.get(LAST_USED_FOLDER, new File(".").getAbsolutePath())
        );
                
        fc.setFileSelectionMode(type);
        //FileFilter filter =  new FileNameExtensionFilter("Text File", "txt");
        //fc.setFileFilter(filter);
        FileFilter filter = new FileNameExtensionFilter("JSON File", "json");
        fc.setFileFilter(filter);
        
        return fc;        
    }
    
    private boolean isAutoLayoutRunning()
    {
        if (this.model.getAutoLayout().isRunning())
        {
            JOptionPane.showMessageDialog(this, "Unable to change settings while trains are running.");
            loadAutoLayoutSettings();
            return true;
        }
        
        return false;
    }
    
    /**
     * Loads all current settings into the UI
     */
    private void loadAutoLayoutSettings()
    {
        this.minDelay.setValue(this.model.getAutoLayout().getMinDelay());
        this.maxDelay.setValue(this.model.getAutoLayout().getMaxDelay());
        this.defaultLocSpeed.setValue(this.model.getAutoLayout().getDefaultLocSpeed());
        this.preArrivalSpeedReduction.setValue(Double.valueOf(this.model.getAutoLayout().getPreArrivalSpeedReduction() * 100).intValue());
        this.maxLocInactiveSeconds.setValue(Double.valueOf(this.model.getAutoLayout().getMaxLocInactiveSeconds() / 60).intValue());
        this.maxActiveTrains.setValue(this.model.getAutoLayout().getMaxActiveTrains());
        this.simulate.setSelected(this.model.getAutoLayout().isSimulate());
        this.atomicRoutes.setSelected(this.model.getAutoLayout().isAtomicRoutes());
        this.turnOffFunctionsOnArrival.setSelected(this.model.getAutoLayout().isTurnOffFunctionsOnArrival());
        this.turnOnFunctionsOnDeparture.setSelected(this.model.getAutoLayout().isTurnOnFunctionsOnDeparture());
        this.maximumLatency.setValue(this.model.getAutoLayout().getMaxLatency());        
    }
    
    /**
     * Disables the start autonomy button
     */
    public void greyOutAutonomy()
    {
        // Execute graceful stop instead
        if (this.gracefulStop.isEnabled())
        {
            gracefulStopActionPerformed(null);
        }
        
        //this.startAutonomy.setEnabled(false);
        //AltEmergencyStopActionPerformed(null);
    }
    
    /**
     * Updates the shown length of an edge
     * @param e
     * @param graph 
     */
    public void updateEdgeLength(Edge e, Graph graph)
    {
        if (e.getLength() > 0 && this.isShowStationLengthsSelected())
        {
            graph.getEdge(e.getUniqueId()).setAttribute("ui.label", e.getLength());
        }
        else
        {
            graph.getEdge(e.getUniqueId()).removeAttribute("ui.label");
        }
    }
    
    /**
     * Adds an edge to the graph
     * @param e
     * @param graph 
     */
    synchronized public void addEdge(Edge e, Graph graph)
    {
        graph.addEdge(e.getUniqueId(), graph.getNode(e.getStart().getUniqueId()), graph.getNode(e.getEnd().getUniqueId()), true);
        // graph.getEdge(e.getUniqueId()).setAttribute("ui.label", e.getStart().getCurrentLocomotive() != null ?  e.getStart().getCurrentLocomotive().getName() : "" );
        // graph.getEdge(e.getUniqueId()).setAttribute("ui.style", e.getStart().getCurrentLocomotive() != null ? "fill-color: rgb(255,165,0);" : "fill-color: rgb(0,0,0);" );    
        graph.getEdge(e.getUniqueId()).setAttribute("ui.class", "inactive");
        
        updateEdgeLength(e, graph);
    }
        
    /**
     * Updates the visibility of certain points and edges
     */
    synchronized public void updateVisiblePoints()
    {
        if (this.graphViewer == null || !this.model.hasAutoLayout()) return;
        
        Graph g = this.graphViewer.getMainGraph();
        
        for (Point p : this.model.getAutoLayout().getPoints())
        {    
            this.updatePoint(p, g);

            if (this.hideReversing.isSelected() && 
                    (p.isReversing() 
                    && (this.model.getAutoLayout().hasOnlyReversingIncoming(p)
                    || this.model.getAutoLayout().hasOnlyReversingNeighbors(p))
                    )
            )
            {
                g.getNode(p.getUniqueId()).setAttribute("ui.hide"); 
            }
            else if (this.hideInactive.isSelected() && 
                    (!p.isActive()
                    && (this.model.getAutoLayout().hasOnlyInactiveIncoming(p) 
                    || this.model.getAutoLayout().hasOnlyInactiveNeighbors(p))
                    )
            )
            {
                g.getNode(p.getUniqueId()).setAttribute("ui.hide"); 
            }
            else
            {
                g.getNode(p.getUniqueId()).removeAttribute("ui.hide");
            }
        }
        
        // Hides edges, TODO move to a separate function later
        for (Edge e : this.model.getAutoLayout().getEdges())
        {
            this.updateEdgeLength(e, g);
            
            if (g.getNode(e.getEnd().getUniqueId()).getAttribute("ui.hide") != null || 
                    g.getNode(e.getStart().getUniqueId()).getAttribute("ui.hide") != null)
            {
                g.getEdge(e.getUniqueId()).setAttribute("ui.hide");
            }
            else
            {
                g.getEdge(e.getUniqueId()).removeAttribute("ui.hide");
            }   
            
        }
    }
    
    /**
     * Refreshes the text of a point
     * @param p
     * @param graph 
     */
    synchronized public void updatePoint(Point p, Graph graph)
    {
        // Labels for station length and exclusions
        String lengthSuffix = "";
        String additionalStyle = "";
        
        if (p.getMaxTrainLength() > 0 && this.showStationLengths.isSelected())
        {
            lengthSuffix = " (" + p.getMaxTrainLength() + ")";
        }
        
        if (!p.getExcludedLocs().isEmpty() && this.showStationLengths.isSelected())
        {
            additionalStyle = "shadow-mode:plain; shadow-color:rgb(255,102,0); shadow-width: 4; shadow-offset:0;";
        }
        else
        {
            additionalStyle = "shadow-mode:none;";
        }
        
        // Remove locomotive from graph if it was deleted
        if (p.isOccupied() && p.getCurrentLocomotive() != null && !this.model.getLocomotives().contains((MarklinLocomotive) p.getCurrentLocomotive()))
        {
            p.setLocomotive(null);
        }
        
        if (p.isOccupied() && p.getCurrentLocomotive() != null)
        {
            graph.getNode(p.getUniqueId()).setAttribute("ui.label", p.getName() + lengthSuffix + "  [" + p.getCurrentLocomotive().getName() + "]");
            graph.getNode(p.getUniqueId()).setAttribute("ui.class", "occupied");
        }
        else
        {
            graph.getNode(p.getUniqueId()).setAttribute("ui.label", p.getName() + lengthSuffix);
            graph.getNode(p.getUniqueId()).setAttribute("ui.class", "unoccupied");
        }

        // Different styles for stations and non-stations
        if (p.isDestination())
        {
            if (p.isTerminus())
            {
                graph.getNode(p.getUniqueId()).setAttribute("ui.style", "shape: box; size: 20px;");
            }
            else if (p.isReversing())
            {
                graph.getNode(p.getUniqueId()).setAttribute("ui.style", "shape: cross; size: 20px;");
            }
            else
            {
                graph.getNode(p.getUniqueId()).setAttribute("ui.style", "shape: circle; size: 20px;");
            }
        }
        else
        {
            if (p.isReversing())
            {
                graph.getNode(p.getUniqueId()).setAttribute("ui.style", "shape: cross; size: 15px;");
            }
            else
            {
                graph.getNode(p.getUniqueId()).setAttribute("ui.style", "shape: diamond; size: 17px;");
            }
        }
        
        if (p.isActive())
        {
            graph.getNode(p.getUniqueId()).setAttribute("ui.style", "fill-color: rgb(0,0,200);" + additionalStyle);
        }
        else
        {
            graph.getNode(p.getUniqueId()).setAttribute("ui.style", "fill-color: rgb(255,102,0);" + additionalStyle);
        }
        
        // Update locomotive autonomy location labels on the main layout
        if (!this.getLayoutStations(p.getName()).isEmpty() 
                && this.graphViewer != null && this.graphViewer.isVisible() // to prevent them from becoming visible again if the window was closed
        )
        {
            Point destination = this.model.getAutoLayout().getDestination(p.getCurrentLocomotive());
            Point start = this.model.getAutoLayout().getStart(p.getCurrentLocomotive());
            Locomotive current = p.getCurrentLocomotive();
            List<Point> milestones = this.model.getAutoLayout().getReachedMilestones(current);
            
            SwingUtilities.invokeLater(() ->
            {
                // This will exclude locked points
                for (JLabel j : this.getLayoutStations(p.getName()))
                {                    
                    j.setOpaque(true);

                    if (current != null)
                    {
                        j.setText("[" + current.getName().substring(0, Math.min(current.getName().length(), LayoutGrid.LAYOUT_STATION_MAX_LENGTH)) + "]");

                        if (milestones != null)
                        {                                 
                            if (milestones.contains(p))
                            {
                                // Completed parts of the route in black
                                j.setForeground(Color.BLACK);
                            }
                            else
                            {
                                // Pending in red
                                j.setForeground(Color.RED);
                            }    

                            if (p.equals(destination))
                            {
                                // Highlight destination
                                j.setBackground(new Color(255, 255, 0, LayoutGrid.LAYOUT_STATION_OPACITY)); // yellow
                            }
                            else
                            {
                                // Don't display locomotive name at intermediate stations to avoid confusion
                                if (!p.equals(start))
                                {
                                    j.setText(LayoutGrid.LAYOUT_STATION_OCCUPIED);
                                    j.setBackground(new Color(255, 255, 255, LayoutGrid.LAYOUT_STATION_OPACITY));
                                }
                                // Originating station highlighted in green
                                else
                                {
                                    j.setBackground(new Color(131,251,131, LayoutGrid.LAYOUT_STATION_OPACITY));
                                }  
                            }
                        }
                        else
                        {
                            // Stationary locomotive
                            j.setForeground(Color.BLACK);
                            j.setBackground(new Color(255, 255, 255, LayoutGrid.LAYOUT_STATION_OPACITY));
                        }
                    }
                    else
                    {
                        // Empty station
                        j.setText(LayoutGrid.LAYOUT_STATION_EMPTY);

                        j.setForeground(Color.BLACK);
                        j.setBackground(new Color(255, 255, 255, LayoutGrid.LAYOUT_STATION_OPACITY));
                    }

                    j.repaint();
                    j.getParent().revalidate();
                    j.getParent().repaint();
                }          
            });
        }
    }
        
    /**
     * Highlights lock edges on the graph for easier editing
     * @param current
     * @param lockedEdges 
     */
    public void highlightLockedEdges(Edge current, List<Edge> lockedEdges)
    {
        for (Edge e : this.model.getAutoLayout().getEdges())
        {                        
            if (lockedEdges != null && lockedEdges.contains(e))
            {
                this.graphViewer.getMainGraph().getEdge(e.getUniqueId()).setAttribute("ui.class", "lockedpreview");
            }
            // Reset unlocked lock edges
            else
            {
                if (current != null && e.equals(current))
                {
                    this.graphViewer.getMainGraph().getEdge(e.getUniqueId()).setAttribute("ui.class", "completed");
                }
                else
                {
                    this.graphViewer.getMainGraph().getEdge(e.getUniqueId()).setAttribute("ui.class", "inactive");
                }
            }
        }
    }
    
    /**
     * Renders a graph visualization of the automated layout
     */
    synchronized private void renderAutoLayoutGraph()
    {
        if (this.graphViewer != null)
        {
            this.graphViewer.dispose();
        }
        
        // Do we set coordinates manually?
        boolean setPoints = true;
        for (Point p : this.model.getAutoLayout().getPoints())
        {
            if (!p.coordinatesSet() || (p.getX() == 0 && p.getY() == 0))
            {
                this.model.log(p.getName() + " has no coordinate info - enabling auto graph layout.");
                setPoints = false;
                break;
            }
        }
        
        Graph graph = new SingleGraph("Layout Graph"); 
        graphViewer = new GraphViewer(graph, this, !setPoints);

        // Custom stylsheet
        URL resource = TrainControlUI.class.getResource(RESOURCE_PATH + GRAPH_CSS_FILE);
        
        int maxY = 0;
        
        for (Point p : this.model.getAutoLayout().getPoints())
        {
            if (p.coordinatesSet() && p.getY() > maxY)
            {
                maxY = p.getY();
            }
        }
        
        try
        {
            graph.setAttribute("ui.stylesheet", "url('" + resource.toURI() +"')");
            
            // Add dummy points to make dragging new nodes make more sense
            if (this.model.getAutoLayout().getPoints().size() < 4)
            {
                graph.addNode("a");
                graph.getNode("a").setAttribute( "ui.class",  "invis" );
                graph.getNode("a").setAttribute( "x", 0 );
                graph.getNode("a").setAttribute( "y", 0 );
                graph.addNode("b");
                graph.getNode("b").setAttribute( "ui.class",  "invis" );
                graph.getNode("b").setAttribute( "x", 200 );
                graph.getNode("b").setAttribute( "y", 0 );
                graph.addNode("c");
                graph.getNode("c").setAttribute( "ui.class",  "invis" );
                graph.getNode("c").setAttribute( "x", 0 );
                graph.getNode("c").setAttribute( "y", 200 );
                graph.addNode("d");
                graph.getNode("d").setAttribute( "ui.class",  "invis" );
                graph.getNode("d").setAttribute( "x", 200 );
                graph.getNode("d").setAttribute( "y", 200 );
            }

            for (Point p : this.model.getAutoLayout().getPoints())
            {
                graph.addNode(p.getUniqueId());
                
                graph.getNode(p.getUniqueId()).setAttribute("weight", 3);
                
                // Set manual coordinates
                if (setPoints)
                {
                    graph.getNode(p.getUniqueId()).setAttribute("x", p.getX());
                    graph.getNode(p.getUniqueId()).setAttribute("y", p.getY());
                }
                
                updatePoint(p, graph);
            }

            for (Edge e : this.model.getAutoLayout().getEdges())
            {
                addEdge(e, graph);
            }
            
            // Callback fires at the beginning and end of each path
            this.model.getAutoLayout().setCallback("GraphCallback", (List<Edge> edges, Locomotive l, Boolean locked) -> 
            {    
                synchronized(graph)
                {  
                    // Update locomotive panel
                    this.repaintAutoLocListLite();
                    
                    this.repaintTimetable();
                                    
                    for (Edge e : edges)
                    {                        
                        for (Edge e2 : e.getLockEdges())
                        {
                            // Grey out locked-lock edges
                            if (locked)
                            {
                                graph.getEdge(e2.getUniqueId()).setAttribute("ui.class", "locked");
                            }
                            // Reset unlocked lock edges
                            else
                            {
                                graph.getEdge(e2.getUniqueId()).setAttribute("ui.class", "inactive");
                            }
                        }
                    }
                    
                    List<Point> milestones = null;
                    
                    if (l != null)
                    {
                        milestones = this.model.getAutoLayout().getReachedMilestones(l);
                    }
                    
                    // Update edge colors and labels
                    for (Edge e : edges)
                    {
                        // Make active edges red
                        graph.getEdge(e.getUniqueId()).setAttribute("ui.class", locked ? "active" : "inactive");
                        // graph.getEdge(e.getUniqueId()).setAttribute("ui.label", locked ? l.getName() : "");

                        // Update point labels
                        for (Point p : Arrays.asList(e.getStart(), e.getEnd()))    
                        {
                            updatePoint(p, graph);
                                                        
                            // Point reached and route is active
                            if (locked)
                            {
                                if (milestones != null && milestones.contains(p))
                                {
                                    graph.getNode(p.getUniqueId()).setAttribute("ui.class", "completed");
                                }
                                else
                                {
                                    graph.getNode(p.getUniqueId()).setAttribute("ui.class", "active");
                                }
                            }
                        }    
                    }
                    
                    // Mark completed edges green
                    if (milestones != null && locked)
                    {
                        for (int i = 1; i < milestones.size(); i++)
                        {
                            graph.getEdge(Edge.getEdgeUniqueId(milestones.get(i - 1), milestones.get(i)))
                                .setAttribute("ui.class", "completed");
                        }
                    }
                    
                    // Highlight start and destination if path is active
                    if (milestones != null && locked && !edges.isEmpty())
                    {
                        if (!milestones.contains(edges.get(edges.size() - 1).getEnd()))
                        {
                            graph.getNode(edges.get(edges.size() - 1).getEnd().getUniqueId()).setAttribute("ui.class", "end");
                        }
                        
                        if (!milestones.contains(edges.get(0).getStart()))
                        {
                            graph.getNode(edges.get(0).getStart().getUniqueId()).setAttribute("ui.class", "start");
                        }
                    }
                             
                    // Update button visibility
                    if (!this.model.getAutoLayout().isRunning())
                    {
                        this.exportJSON.setEnabled(true);
                        this.gracefulStop.setEnabled(false);
                    }
                    else
                    {
                        this.exportJSON.setEnabled(false);
                        // this.gracefulStop.setEnabled(true);
                    }
                }

                return null;                
            });
            
            this.repaintAutoLocList(false);
            this.repaintTimetable();
            this.updateVisiblePoints();
        } 
        catch (URISyntaxException ex)
        {
            this.model.log("Error loading graph UI.");
        }        
    }
        
    /**
     * Repaints the timetable once a route completes
     */
    synchronized private void repaintTimetable()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            // Initial setup
            if (this.timetable.getColumnCount() != 5)
            {
                // Aggregate stats
                String col[] = {"Index", "Locomotive", "Start", "Destination", "Time"};

                DefaultTableModel tableModel = new DefaultTableModel(col, 0)
                {
                    // Disable editing
                    @Override
                    public boolean isCellEditable(int row, int column)
                    {  
                        return false;  
                    }
                };
                
                this.timetable.setModel(tableModel);
                TableRowSorter<TableModel> sorter = new TableRowSorter<>(this.timetable.getModel());
                sorter.setSortable(0, false); 
                sorter.setSortable(1, false); 
                sorter.setSortable(2, false); 
                sorter.setSortable(3, false); 
                sorter.setSortable(4, false); 
                this.timetable.setRowSorter(sorter);
            }
            
            // Update the data
            this.timetableCapture.setSelected(this.model.getAutoLayout().isTimetableCapture());
            
            List<TimetablePath> timeTable = this.model.getAutoLayout().getTimetable();
            
            // Do nothing if the timetable hasn't been updated
            if (timeTable.hashCode() == lastTimetableState) return;
            
            lastTimetableState = timeTable.hashCode();
            
            DefaultTableModel tableModel = (DefaultTableModel) this.timetable.getModel();
            tableModel.setRowCount(0);
                        
            for (TimetablePath path : timeTable)
            {
                Object[] data = {tableModel.getRowCount() + 1, path.getLoc().getName(), path.getStart().getName(), path.getEnd().getName(),
                path.isExecuted() ? Conversion.convertSecondsToDatetime(path.getExecutionTime()) : "Pending Start +" + (path.getSecondsToNext() / 1000) + "s"};

                tableModel.addRow(data);
            }
        }));
    }
    
    synchronized public void repaintAutoLocList(boolean external)
    {
        if (this.model.hasAutoLayout() && this.model.getAutoLayout().isValid())
        {
            // If called from another UI component, no need to refresh if there are no trains or if autonomy is on
            if (external)
            {
                if (this.model.getAutoLayout().getPoints().isEmpty()) return;
                if (this.model.getAutoLayout().isAutoRunning() && !this.model.getAutoLayout().getActiveLocomotives().isEmpty()) return;
                
                // Prevent concurrent calls
                for (Future<?> f : this.autonomyFutures)
                {
                    if (!f.isDone()) 
                    {
                        // this.model.log("Auto UI refresh in progress");
                        return;
                    }
                }

                // this.model.log("Refreshing auto UI");
                this.autonomyFutures.clear();

                this.autonomyFutures.add(
                    this.AutonomyRenderer.submit(new Thread(() -> 
                    {
                        try 
                        {
                            Thread.sleep(REPAINT_ROUTE_INTERVAL);
                        } catch (InterruptedException ex) { }
                        
                        this.repaintAutoLocListLite();
                    }))
                );
            }
            else
            {
                this.repaintAutoLocListFull();
            }
        }
    }
    
    /**
     * Repaints the auto locomotive list paths only
     */
    synchronized private void repaintAutoLocListLite()
    { 
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            new Thread(() ->
            {
                for (Object o : this.autoLocPanel.getComponents())
                {
                    AutoLocomotiveStatus status = (AutoLocomotiveStatus) o;
                    status.updateState(null);
                }
            }).start();
        }));
    }
    
    /**
     * Fully repaints the auto locomotive list based on auto layout state
     */
    synchronized private void repaintAutoLocListFull()
    { 
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            // Display locomotive status and possible paths
            this.autoLocPanel.removeAll();

            // Number of columns in the grid
            int gridCols = 3;

            autoLocPanel.setLayout(new java.awt.GridLayout(
                (int) Math.ceil((double) this.model.getAutoLayout().getLocomotivesToRun().size() / gridCols), 
                gridCols, // cols
                5, // padding
                5)
            );
            
            // Sort alphabetically, with parked locomotives last
            List<Locomotive> locs = new LinkedList<>(this.model.getAutoLayout().getLocomotivesToRun());
            
            locs.sort((Locomotive l1, Locomotive l2) ->
            {
                Point loc1Point = this.model.getAutoLayout().getLocomotiveLocation(l1);
                Point loc2Point = this.model.getAutoLayout().getLocomotiveLocation(l2);
                
                if (loc1Point != null && loc2Point != null)
                {
                    if (loc1Point.isActive() && !loc2Point.isActive()) return -1;
                    if (!loc1Point.isActive() && loc2Point.isActive()) return 1;
                }
                else if (loc1Point == null)
                {
                    return 1;
                }
                else if (loc2Point == null)
                {
                    return -1;
                }            
                
                return l1.getName().compareTo(l2.getName());
            });
            
            for (Locomotive loc : locs)
            {
                this.autoLocPanel.add(new AutoLocomotiveStatus(loc, this));
            }
            
            // Speed up scrolling
            jScrollPane4.getVerticalScrollBar().setUnitIncrement(16);

            // Sometimes the list doesn't repaint until you click on it.  Alernative might be to do this before rendering the graph.
            this.autoLocPanel.repaint();
            this.locCommandPanels.repaint();
        }));
    }

    @Override
    public void emergencyStopTriggered(Route r)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            JOptionPane.showMessageDialog(this, "The power has been automatically turned off because route " + r.getName() + " was triggered.");
        }));
    }
         
    public class CustomTableRenderer extends DefaultTableCellRenderer
    {
        // Support hover events
        private int hoveredRow = -1;
        private int hoveredColumn = -1;
        
        public void setHoveredCell(int row, int column)
        {
            hoveredRow = row;
            hoveredColumn = column;
        }
        
        public void resetHoveredCell()
        {
            hoveredRow = -1;
            hoveredColumn = -1;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            // Hover effect
            JLabel j = (JLabel) c;
            
            if (row == hoveredRow && column == hoveredColumn && value != null)
            {
                j.setBackground(new Color(240,240,240));
            }
            else
            {
                j.setBackground(Color.WHITE);
            }
          
            // Determine the border to set
            int top = (row == 0) ? 1 : 0;
            int left = (column == 0) ? 1 : 0;
            int bottom = 1;
            int right = 1;

            j.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));
            // End hover effect
            
            if (value != null)
            {
                String name = ((MarklinRoute) value).getName();
                
                // Show route ID if sorting by ID
                if (prefs.getBoolean(ROUTE_SORT_PREF, false))
                {
                    setText(name);
                }
                else
                {
                    setText(model.getRouteId(name) + ". " + name);
                }
                
                // Differentiate central station routes
                if (model.getRoute(name).isLocked())
                {
                    setText(getText() + " *");
                    // setToolTipText("Route from Central Station.");
                }
                else
                {
                    // setToolTipText("");
                }
                
                // Add padding due to hover effect
                setText(" " + getText());
                
                if (model.getRoute(name).isEnabled() && model.getRoute(name).hasS88())
                {
                    // set to red bold font
                    c.setForeground(Color.RED);
                    c.setFont(new Font("Dialog", Font.BOLD, 12));
                } 
                else 
                {
                    // stay at default
                    c.setForeground(Color.BLACK);
                    c.setFont(new Font("Dialog", Font.PLAIN, 12));
                }
            }
  
            return c;
        }
    }
    
    public void refreshRouteList()
    {   
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            DefaultTableModel tableModel = new DefaultTableModel()
            {
                @Override
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }
            };

            tableModel.setColumnCount(ROUTE_UI_COLS);

            Object[] items = new Object[ROUTE_UI_COLS];

            List<String> names = this.model.getRouteList();

            if (prefs.getBoolean(ROUTE_SORT_PREF, false))
            {
                Collections.sort(names);
            }
            
            // Collect the objects to store in the grid - no more strings
            List<MarklinRoute> routes = names.stream().map(this.model::getRoute).collect(Collectors.toList());

            int i = 0;
            while (!routes.isEmpty())
            {
                items[i++] = routes.get(0);
                routes.remove(0);

                if (i == ROUTE_UI_COLS || routes.isEmpty())
                {
                    i = 0;

                    tableModel.addRow(items);

                    items = new Object[ROUTE_UI_COLS];
                }
            }

            this.RouteList.setModel(tableModel);
            this.RouteList.setShowGrid(true);  
            
            for (int j = 0; j < ROUTE_UI_COLS; j++)
            {
                this.RouteList.getColumnModel().getColumn(j).setCellRenderer(new CustomTableRenderer());
            }

            // this.RouteList.setToolTipText("Left click route to execute, right click to edit");
        }));
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AButton;
    private javax.swing.JTextField ALabel;
    private javax.swing.JSlider ASlider;
    private javax.swing.JLabel ActiveLocLabel;
    private javax.swing.JButton AddRouteButton;
    private javax.swing.JButton AltEmergencyStop;
    private javax.swing.JCheckBoxMenuItem AutoLoadAutonomyMenuItem;
    private javax.swing.JButton BButton;
    private javax.swing.JTextField BLabel;
    private javax.swing.JSlider BSlider;
    private javax.swing.JToggleButton Backward;
    private javax.swing.JButton BulkDisable;
    private javax.swing.JButton BulkEnable;
    private javax.swing.JButton CButton;
    private javax.swing.JTextField CLabel;
    private javax.swing.JSlider CSlider;
    private javax.swing.JLabel CurrentKeyLabel;
    private javax.swing.JButton DButton;
    private javax.swing.JRadioButton DCC;
    private javax.swing.JTextField DLabel;
    private javax.swing.JSlider DSlider;
    private javax.swing.JLabel DirectionLabel;
    private javax.swing.JButton DownArrow;
    private javax.swing.JButton EButton;
    private javax.swing.JTextField ELabel;
    private javax.swing.JSlider ESlider;
    private javax.swing.JLabel EStopLabel;
    private javax.swing.JButton EightButton;
    private javax.swing.JToggleButton F0;
    private javax.swing.JToggleButton F1;
    private javax.swing.JToggleButton F10;
    private javax.swing.JToggleButton F11;
    private javax.swing.JToggleButton F12;
    private javax.swing.JToggleButton F13;
    private javax.swing.JToggleButton F14;
    private javax.swing.JToggleButton F15;
    private javax.swing.JToggleButton F16;
    private javax.swing.JToggleButton F17;
    private javax.swing.JToggleButton F18;
    private javax.swing.JToggleButton F19;
    private javax.swing.JToggleButton F2;
    private javax.swing.JToggleButton F20;
    private javax.swing.JPanel F20AndUpPanel;
    private javax.swing.JToggleButton F21;
    private javax.swing.JToggleButton F22;
    private javax.swing.JToggleButton F23;
    private javax.swing.JToggleButton F24;
    private javax.swing.JToggleButton F25;
    private javax.swing.JToggleButton F26;
    private javax.swing.JToggleButton F27;
    private javax.swing.JToggleButton F28;
    private javax.swing.JToggleButton F29;
    private javax.swing.JToggleButton F3;
    private javax.swing.JToggleButton F30;
    private javax.swing.JToggleButton F31;
    private javax.swing.JToggleButton F4;
    private javax.swing.JToggleButton F5;
    private javax.swing.JToggleButton F6;
    private javax.swing.JToggleButton F7;
    private javax.swing.JToggleButton F8;
    private javax.swing.JToggleButton F9;
    private javax.swing.JButton FButton;
    private javax.swing.JTextField FLabel;
    private javax.swing.JSlider FSlider;
    private javax.swing.JButton FiveButton;
    private javax.swing.JToggleButton Forward;
    private javax.swing.JButton FourButton;
    private javax.swing.JLabel FullSpeedLabel;
    private javax.swing.JTabbedPane FunctionTabs;
    private javax.swing.JButton GButton;
    private javax.swing.JTextField GLabel;
    private javax.swing.JSlider GSlider;
    private javax.swing.JButton HButton;
    private javax.swing.JTextField HLabel;
    private javax.swing.JSlider HSlider;
    private javax.swing.JButton IButton;
    private javax.swing.JTextField ILabel;
    private javax.swing.JSlider ISlider;
    private javax.swing.JPanel InnerLayoutPanel;
    private javax.swing.JButton JButton;
    private javax.swing.JTextField JLabel;
    private javax.swing.JSlider JSlider;
    private javax.swing.JButton KButton;
    private javax.swing.JTextField KLabel;
    private javax.swing.JSlider KSlider;
    private javax.swing.JLabel KeyboardLabel;
    private javax.swing.JLabel KeyboardLabel1;
    private javax.swing.JLabel KeyboardNumberLabel;
    private javax.swing.JPanel KeyboardPanel;
    private javax.swing.JTabbedPane KeyboardTab;
    private javax.swing.JButton LButton;
    private javax.swing.JTextField LLabel;
    private javax.swing.JSlider LSlider;
    private javax.swing.JScrollPane LayoutArea;
    private javax.swing.JComboBox LayoutList;
    private javax.swing.JButton LeftArrow;
    private javax.swing.JPanel LocContainer;
    private javax.swing.JPanel LocControlPanel;
    private javax.swing.JPanel LocFunctionsPanel;
    private javax.swing.JLabel LocMappingNumberLabel;
    private javax.swing.JButton MButton;
    private javax.swing.JTextField MLabel;
    private javax.swing.JRadioButton MM2;
    private javax.swing.JSlider MSlider;
    private javax.swing.JButton NButton;
    private javax.swing.JTextField NLabel;
    private javax.swing.JSlider NSlider;
    private javax.swing.JButton NextKeyboard;
    private javax.swing.JButton NextLocMapping;
    private javax.swing.JButton NineButton;
    private javax.swing.JButton OButton;
    private javax.swing.JTextField OLabel;
    private javax.swing.JSlider OSlider;
    private javax.swing.JButton OnButton;
    private javax.swing.JButton OneButton;
    private javax.swing.JButton PButton;
    private javax.swing.JTextField PLabel;
    private javax.swing.JSlider PSlider;
    private javax.swing.JButton PowerOff;
    private javax.swing.JButton PrevKeyboard;
    private javax.swing.JButton PrevLocMapping;
    private javax.swing.JLabel PrimaryControls;
    private javax.swing.JButton QButton;
    private javax.swing.JTextField QLabel;
    private javax.swing.JSlider QSlider;
    private javax.swing.JButton RButton;
    private javax.swing.JTextField RLabel;
    private javax.swing.JSlider RSlider;
    private javax.swing.JButton RightArrow;
    private javax.swing.JTable RouteList;
    private javax.swing.JPanel RoutePanel;
    private javax.swing.JButton SButton;
    private javax.swing.JTextField SLabel;
    private javax.swing.JSlider SSlider;
    private javax.swing.JButton SevenButton;
    private javax.swing.JButton ShiftButton;
    private javax.swing.JButton SixButton;
    private javax.swing.JComboBox SizeList;
    private javax.swing.JLabel SlowStopLabel;
    private javax.swing.JButton SpacebarButton;
    private javax.swing.JSlider SpeedSlider;
    private javax.swing.JToggleButton SwitchButton1;
    private javax.swing.JToggleButton SwitchButton10;
    private javax.swing.JToggleButton SwitchButton11;
    private javax.swing.JToggleButton SwitchButton12;
    private javax.swing.JToggleButton SwitchButton13;
    private javax.swing.JToggleButton SwitchButton14;
    private javax.swing.JToggleButton SwitchButton15;
    private javax.swing.JToggleButton SwitchButton16;
    private javax.swing.JToggleButton SwitchButton17;
    private javax.swing.JToggleButton SwitchButton18;
    private javax.swing.JToggleButton SwitchButton19;
    private javax.swing.JToggleButton SwitchButton2;
    private javax.swing.JToggleButton SwitchButton20;
    private javax.swing.JToggleButton SwitchButton21;
    private javax.swing.JToggleButton SwitchButton22;
    private javax.swing.JToggleButton SwitchButton23;
    private javax.swing.JToggleButton SwitchButton24;
    private javax.swing.JToggleButton SwitchButton25;
    private javax.swing.JToggleButton SwitchButton26;
    private javax.swing.JToggleButton SwitchButton27;
    private javax.swing.JToggleButton SwitchButton28;
    private javax.swing.JToggleButton SwitchButton29;
    private javax.swing.JToggleButton SwitchButton3;
    private javax.swing.JToggleButton SwitchButton30;
    private javax.swing.JToggleButton SwitchButton31;
    private javax.swing.JToggleButton SwitchButton32;
    private javax.swing.JToggleButton SwitchButton33;
    private javax.swing.JToggleButton SwitchButton34;
    private javax.swing.JToggleButton SwitchButton35;
    private javax.swing.JToggleButton SwitchButton36;
    private javax.swing.JToggleButton SwitchButton37;
    private javax.swing.JToggleButton SwitchButton38;
    private javax.swing.JToggleButton SwitchButton39;
    private javax.swing.JToggleButton SwitchButton4;
    private javax.swing.JToggleButton SwitchButton40;
    private javax.swing.JToggleButton SwitchButton41;
    private javax.swing.JToggleButton SwitchButton42;
    private javax.swing.JToggleButton SwitchButton43;
    private javax.swing.JToggleButton SwitchButton44;
    private javax.swing.JToggleButton SwitchButton45;
    private javax.swing.JToggleButton SwitchButton46;
    private javax.swing.JToggleButton SwitchButton47;
    private javax.swing.JToggleButton SwitchButton48;
    private javax.swing.JToggleButton SwitchButton49;
    private javax.swing.JToggleButton SwitchButton5;
    private javax.swing.JToggleButton SwitchButton50;
    private javax.swing.JToggleButton SwitchButton51;
    private javax.swing.JToggleButton SwitchButton52;
    private javax.swing.JToggleButton SwitchButton53;
    private javax.swing.JToggleButton SwitchButton54;
    private javax.swing.JToggleButton SwitchButton55;
    private javax.swing.JToggleButton SwitchButton56;
    private javax.swing.JToggleButton SwitchButton57;
    private javax.swing.JToggleButton SwitchButton58;
    private javax.swing.JToggleButton SwitchButton59;
    private javax.swing.JToggleButton SwitchButton6;
    private javax.swing.JToggleButton SwitchButton60;
    private javax.swing.JToggleButton SwitchButton61;
    private javax.swing.JToggleButton SwitchButton62;
    private javax.swing.JToggleButton SwitchButton63;
    private javax.swing.JToggleButton SwitchButton64;
    private javax.swing.JToggleButton SwitchButton7;
    private javax.swing.JToggleButton SwitchButton8;
    private javax.swing.JToggleButton SwitchButton9;
    private javax.swing.JButton TButton;
    private javax.swing.JTextField TLabel;
    private javax.swing.JSlider TSlider;
    private javax.swing.JButton ThreeButton;
    private javax.swing.JButton TwoButton;
    private javax.swing.JButton UButton;
    private javax.swing.JTextField ULabel;
    private javax.swing.JSlider USlider;
    private javax.swing.JButton UpArrow;
    private javax.swing.JButton VButton;
    private javax.swing.JTextField VLabel;
    private javax.swing.JSlider VSlider;
    private javax.swing.JButton WButton;
    private javax.swing.JTextField WLabel;
    private javax.swing.JSlider WSlider;
    private javax.swing.JButton XButton;
    private javax.swing.JTextField XLabel;
    private javax.swing.JSlider XSlider;
    private javax.swing.JButton YButton;
    private javax.swing.JTextField YLabel;
    private javax.swing.JSlider YSlider;
    private javax.swing.JButton ZButton;
    private javax.swing.JTextField ZLabel;
    private javax.swing.JSlider ZSlider;
    private javax.swing.JButton ZeroButton;
    private javax.swing.JLabel ZeroPercentSpeedLabel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JCheckBoxMenuItem activeLocInTitle;
    private javax.swing.JMenuItem addBlankPageMenuItem;
    private javax.swing.JMenuItem addLocomotiveMenuItem;
    private javax.swing.JButton allButton;
    private javax.swing.JCheckBox atomicRoutes;
    private javax.swing.JPanel autoLocPanel;
    private javax.swing.JPanel autoPanel;
    private javax.swing.JPanel autoSettingsPanel;
    private javax.swing.JTextArea autonomyJSON;
    private javax.swing.JPanel autonomyPanel;
    private javax.swing.JCheckBox autosave;
    private javax.swing.JMenuItem backupDataMenuItem;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JMenuItem changeIPMenuItem;
    private javax.swing.JCheckBoxMenuItem checkForUpdates;
    private javax.swing.JMenuItem chooseLocalDataFolderMenuItem;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JTextArea debugArea;
    private javax.swing.JSlider defaultLocSpeed;
    private javax.swing.JMenuItem deleteLayoutMenuItem;
    private javax.swing.JMenuItem downloadCSLayoutMenuItem;
    private javax.swing.JMenuItem downloadUpdateMenuItem;
    private javax.swing.JMenuItem duplicateLayoutMenuItem;
    private javax.swing.JMenuItem editCurrentPageActionPerformed;
    private javax.swing.JButton editLayoutButton;
    private javax.swing.JButton executeTimetable;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton exportJSON;
    private javax.swing.JMenuItem exportRoutesMenuItem;
    private javax.swing.JLabel f0Label;
    private javax.swing.JLabel f10Label;
    private javax.swing.JLabel f11Label;
    private javax.swing.JLabel f12Label;
    private javax.swing.JLabel f13Label;
    private javax.swing.JLabel f14Label;
    private javax.swing.JLabel f15Label;
    private javax.swing.JLabel f16Label;
    private javax.swing.JLabel f17Label;
    private javax.swing.JLabel f18Label;
    private javax.swing.JLabel f19Label;
    private javax.swing.JLabel f1Label;
    private javax.swing.JLabel f20Label;
    private javax.swing.JLabel f21Label;
    private javax.swing.JLabel f22Label;
    private javax.swing.JLabel f23Label;
    private javax.swing.JLabel f24Label;
    private javax.swing.JLabel f25Label;
    private javax.swing.JLabel f26Label;
    private javax.swing.JLabel f27Label;
    private javax.swing.JLabel f28Label;
    private javax.swing.JLabel f29Label;
    private javax.swing.JLabel f2Label;
    private javax.swing.JLabel f30Label;
    private javax.swing.JLabel f31Label;
    private javax.swing.JLabel f3Label;
    private javax.swing.JLabel f4Label;
    private javax.swing.JLabel f5Label;
    private javax.swing.JLabel f6Label;
    private javax.swing.JLabel f7Label;
    private javax.swing.JLabel f8Label;
    private javax.swing.JLabel f9Label;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel functionPanel;
    private javax.swing.JMenu functionsMenu;
    private javax.swing.JButton gracefulStop;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JCheckBox hideInactive;
    private javax.swing.JCheckBox hideReversing;
    private javax.swing.JMenuItem importRoutesMenuItem;
    private javax.swing.JMenuItem initializeLocalLayoutMenuItem;
    private javax.swing.JMenu interfaceMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator20;
    private javax.swing.JPopupMenu.Separator jSeparator21;
    private javax.swing.JPopupMenu.Separator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JButton jsonDocumentationButton;
    private javax.swing.JRadioButtonMenuItem keyboardAzertyMenuItem;
    private javax.swing.JPanel keyboardButtonPanel;
    private javax.swing.JRadioButtonMenuItem keyboardQwertyMenuItem;
    private javax.swing.JRadioButtonMenuItem keyboardQwertzMenuItem;
    private javax.swing.JLabel latencyLabel;
    private javax.swing.JLabel layoutListLabel;
    private javax.swing.JMenu layoutMenu;
    private javax.swing.JMenu layoutMenuItem;
    private javax.swing.JButton layoutNewWindow;
    private javax.swing.JPanel layoutPanel;
    private javax.swing.JButton loadDefaultBlankGraph;
    private javax.swing.JButton loadJSONButton;
    private javax.swing.JTabbedPane locCommandPanels;
    private javax.swing.JPanel locCommandTab;
    private javax.swing.JLabel locIcon;
    private javax.swing.JLabel locMappingLabel;
    private javax.swing.JMenu locomotiveControlMenu;
    private javax.swing.JMenu locomotiveMenu;
    private javax.swing.JPanel logPanel;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JSlider maxActiveTrains;
    private javax.swing.JSlider maxDelay;
    private javax.swing.JSlider maxLocInactiveSeconds;
    private javax.swing.JSlider maximumLatency;
    private javax.swing.JCheckBoxMenuItem menuItemShowLayoutAddresses;
    private javax.swing.JSlider minDelay;
    private javax.swing.JMenu modifyLocalLayoutMenu;
    private javax.swing.JMenuItem openCS3AppMenuItem;
    private javax.swing.JMenuItem openLegacyTrackDiagramEditor;
    private javax.swing.JRadioButtonMenuItem powerNoChangeStartup;
    private javax.swing.JRadioButtonMenuItem powerOffStartup;
    private javax.swing.JRadioButtonMenuItem powerOnStartup;
    private javax.swing.JSlider preArrivalSpeedReduction;
    private javax.swing.JMenuItem quickFindMenuItem;
    private javax.swing.JCheckBoxMenuItem rememberLocationMenuItem;
    private javax.swing.JMenuItem renameLayoutMenuItem;
    private javax.swing.JMenu routesMenu;
    private javax.swing.JMenuItem showCurrentLayoutFolderMenuItem;
    private javax.swing.JCheckBoxMenuItem showKeyboardHintsMenuItem;
    private javax.swing.JCheckBox showStationLengths;
    private javax.swing.JCheckBox simulate;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JCheckBoxMenuItem slidersChangeActiveLocMenuItem;
    private javax.swing.JButton smallButton;
    private javax.swing.JRadioButton sortByID;
    private javax.swing.JRadioButton sortByName;
    private javax.swing.JButton startAutonomy;
    private javax.swing.JMenuItem switchCSLayoutMenuItem;
    private javax.swing.JMenuItem syncFullLocStateMenuItem;
    private javax.swing.JMenuItem syncMenuItem;
    private javax.swing.JTable timetable;
    private javax.swing.JToggleButton timetableCapture;
    private javax.swing.JPanel timetablePanel;
    private javax.swing.JCheckBox toggleMenuBar;
    private javax.swing.JMenuItem turnOffFunctionsMenuItem;
    private javax.swing.JCheckBox turnOffFunctionsOnArrival;
    private javax.swing.JCheckBox turnOnFunctionsOnDeparture;
    private javax.swing.JMenuItem turnOnLightsMenuItem;
    private javax.swing.JButton validateButton;
    private javax.swing.JMenuItem viewDatabaseMenuItem;
    private javax.swing.JMenuItem viewReleasesMenuItem;
    private javax.swing.JCheckBoxMenuItem windowAlwaysOnTopMenuItem;
    // End of variables declaration//GEN-END:variables

    // Lap strings in the size dropdown to icon sizes
    Map<String, Integer> layoutSizes = Stream.of(new String[][] {
        { "Small", "30" }, 
        { "Large", "60" }, 
      }).collect(Collectors.toMap(data -> data[0], data -> Integer.valueOf(data[1])));
    
    /**
     * Checks if layout files are being loaded from the local filesystem
     * @return 
     */
    private boolean isLocalLayout()
    {
        return !"".equals(prefs.get(LAYOUT_OVERRIDE_PATH_PREF, ""));
    }
    
    private String getLocalLayoutPath()
    {
        return prefs.get(LAYOUT_OVERRIDE_PATH_PREF, null);
    }
    
    private void showFileExplorer(File path)
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            try
            {
                Desktop.getDesktop().open(path);
            }
            catch (Exception e)
            {
                this.model.log("Could not open file explorer.");

                if (this.model.isDebug())
                {
                    this.model.log(e);
                }
            }
        }));
    }
    
    /**
     * Gets the active layout path
     * @param showFolder
     * @return 
     */
    public String getLayoutPath(boolean showFolder)
    {
        if (!isLocalLayout())
        {
            String url = "";
            
            if (!this.model.getLayoutList().isEmpty() && showFolder)
            {
                url = this.model.getLayout((String) this.LayoutList.getSelectedItem()).getUrl();
                
                Util.openUrl(url);
                
                url = "\n" + url;
            }
            
            return "Layout from Central Station:\n" + prefs.get(IP_PREF, "(None loaded)") + url;
        }
        else
        {
            String path = prefs.get(LAYOUT_OVERRIDE_PATH_PREF, "");
            
            if (!path.isEmpty() && showFolder)
            {
                showFileExplorer(new File(path));
            }
            
            return "Local Layout Files:\n" + (path.isEmpty() ? "(None loaded)" : path);
        }
    }
    
    /**
     * Repaints the label with the layout file path
     */
    public void repaintPathLabel()
    {
        javax.swing.SwingUtilities.invokeLater(new Thread(() ->
        {
            // Set UI label
            if (!isLocalLayout())
            {
                // LayoutPathLabel.setText("Central Station: " + prefs.get(IP_PREF, "(none loaded)"));
                this.switchCSLayoutMenuItem.setEnabled(false);
                this.modifyLocalLayoutMenu.setEnabled(false);
                this.downloadCSLayoutMenuItem.setEnabled(!this.model.getLayoutList().isEmpty());
            }
            else
            {
                // LayoutPathLabel.setText(prefs.get(LAYOUT_OVERRIDE_PATH_PREF, ""));
                this.switchCSLayoutMenuItem.setEnabled(true);
                this.modifyLocalLayoutMenu.setEnabled(true);
                this.downloadCSLayoutMenuItem.setEnabled(false);
            }
        }));
    }
    
    @Override
    public synchronized void repaintLayout()
    {
        repaintLayout(false, false);
    }  
    
    public AddLocomotive getLocAdder()
    {
        return adder;
    }
    
    /**
     * Repaints layout from cache - called from UI dropdown/hotkeys
     */
    public synchronized void repaintLayoutFromCache()
    {
        repaintLayout(false, true);
    }    
    
    /**
     * Repaints the track diagram
     * @param showTab - do we focus the layout tab?
     * @param useCache - display cached version of the layout?
     */
    public synchronized void repaintLayout(boolean showTab, boolean useCache)
    {    
        this.LayoutGridRenderer.submit(new Thread(() -> 
        { 
            javax.swing.SwingUtilities.invokeLater(new Thread(() ->
            {
                repaintPathLabel();

                if (this.model.getLayoutList().isEmpty())
                {
                    this.KeyboardTab.setEnabledAt(1, false);
                    if (this.KeyboardTab.getSelectedIndex() == 1)
                    {
                        this.KeyboardTab.setSelectedIndex(0);
                    }
                    // this.KeyboardTab.remove(this.layoutPanel);
                }
                else 
                {
                    this.KeyboardTab.setEnabledAt(1, true);
                    /*if (!this.KeyboardTab.getTitleAt(1).contains("Track Diagram"))
                    {
                        this.KeyboardTab.add(this.layoutPanel, 1);
                        this.KeyboardTab.setTitleAt(1, "Track Diagram");     
                    };*/

                    //InnerLayoutPanel.setVisible(false);
                    String cacheKey = this.LayoutList.getSelectedItem().toString() + " " + this.SizeList.getSelectedItem().toString();

                    if (useCache && this.layoutCache.containsKey(cacheKey))
                    {
                        InnerLayoutPanel.removeAll();
                        
                        InnerLayoutPanel.add(this.layoutCache.get(cacheKey));
                                                
                        if (this.model.isDebug())
                        {
                            this.model.log("Used cache to paint " + cacheKey);
                        }
                    }
                    else
                    {
                        // Set address label preference
                        this.model.getLayout(this.LayoutList.getSelectedItem().toString()).setShowAddress(this.getShowLayoutAddresses());
                        
                        this.trainGrid = new LayoutGrid(
                            this.model.getLayout(this.LayoutList.getSelectedItem().toString()), 
                            this.layoutSizes.get(this.SizeList.getSelectedItem().toString()), 
                            InnerLayoutPanel, 
                            KeyboardTab, 
                            false,
                            this
                        );
                        
                        if (this.model.isDebug())
                        {
                            this.model.log("Repainted layout " + cacheKey);
                        }
                        
                        // Reset cache
                        if (!useCache)
                        {
                            this.layoutCache = new HashMap<>();
                        }

                        if (this.trainGrid.isCacheable())
                        {
                            this.layoutCache.put(cacheKey, this.trainGrid.getContainer());
                        }
                    }
                    
                    InnerLayoutPanel.setVisible(true);

                    // Important!
                    this.KeyboardTab.repaint();
                    
                    if (showTab) this.KeyboardTab.setSelectedIndex(1);
                    
                    // Update auto layout station labels on the track diagram
                    this.updateVisiblePoints();
                }
            }));
        }));
    }
}
