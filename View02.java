import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import java.io.*;

public class View02 {
//-----------------------------------------------------------------------------------------------------------------------------------
	public class InterPanel extends JPanel {
		public void paintComponent(Graphics g){
			g.fillRect(0,0,this.getWidth(),this.getHeight());
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------
	public class MyPanel extends JPanel {
		ArrayList<Connection> connections = new ArrayList<Connection>(0);
		HashMap<String, InterPanel> interfaces = new HashMap<String, InterPanel>(0);

		public MyPanel() {


		}

		public void initConnections() {
			Set<String> ipSet = model.map.keySet();
			HashSet<String> done = new HashSet<String>(0);

			for (String src : ipSet) {
				done.add(src);
				if (!(interfaces.containsKey(src))) {
					interfaces.put(src, new InterPanel());
					this.add(interfaces.get(src), FlowLayout.LEFT);
				}
				Set<String> destSet = model.map.get(src).keySet();
				for(String dest : destSet) {
					if (!(done.contains(dest))) {
						//create new connection in connections-list
						connections.add(new Connection(src, dest, model.map.get(src).get(dest)	 ));
					}
				}
			}
		}

		public void paintComponent(Graphics g){
			//g.fillRect(0,0,this.getWidth(),this.getHeight());
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------

	AddressDatabase model = new AddressDatabase();
	File file;

	JFrame frame = new JFrame();

	JMenuBar mBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenuItem save = new JMenuItem("Save");
	JMenuItem saveAs = new JMenuItem("Save As");
	JMenuItem load = new JMenuItem("Load");

	JPanel panelx = new JPanel();
	JLabel ttlL = new JLabel("TTL");
	JTextField ttlT = new JTextField(4);
	JLabel waitL = new JLabel("Wait");
	JTextField waitT = new JTextField(4);
	JLabel targetL = new JLabel("Target");
	JTextField targetT = new JTextField(15);
	JButton button = new JButton("Trace Route");

	JSplitPane centrePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	MyPanel panel = new MyPanel();
	JSplitPane sidePane = new JSplitPane(0);

	JList<String> listSRC = new JList<String>();
	JList<String> listDEST = new JList<String>();
	JScrollPane northS = new JScrollPane(listSRC);
	JScrollPane southS = new JScrollPane(listDEST);

	public void init() {

		frame.setSize(1200,700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mBar.add(fileMenu);
		fileMenu.add(save);
		save.addActionListener(new SaveActionListener());
		fileMenu.add(saveAs);
		saveAs.addActionListener(new SaveAsActionListener());
		fileMenu.add(load);
		load.addActionListener(new LoadActionListener());

		panelx.add(ttlL);
		panelx.add(ttlT);
		panelx.add(waitL);
		panelx.add(waitT);
		panelx.add(targetL);
		panelx.add(targetT);
		panelx.add(button);

		button.addActionListener(new TraceActionListener());

		frame.setJMenuBar(mBar);
		frame.getContentPane().add(centrePane, BorderLayout.CENTER);
		frame.getContentPane().add(panelx, BorderLayout.SOUTH);

		centrePane.setOneTouchExpandable(true);
		centrePane.add(panel, JSplitPane.LEFT, 0);
		centrePane.add(sidePane, JSplitPane.RIGHT, 0);

		northS.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		southS.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		sidePane.setOneTouchExpandable(true);
		sidePane.add(northS, JSplitPane.TOP, 0);
		sidePane.add(southS, JSplitPane.BOTTOM, 0);

		listSRC.addMouseListener(new SrcListMouseListener());
		listSRC.setFixedCellHeight(40);
		listDEST.setFixedCellHeight(20);

		frame.setVisible(true);
		centrePane.setDividerLocation(0.80);
		sidePane.setDividerLocation(0.75);

		//panel.add(new InterPanel(), FlowLayout.LEFT);
		//System.out.println(panel.getLayout());
	}

	public void saveAs(){
		try {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog(frame);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				FileWriter writer = new FileWriter(file);
				writer.write(model.saveIt());
				writer.close();
			} 
		} catch(IOException ex) {System.out.println("error in saveAs()");}
	}

	public void save(){
		try {
			JFileChooser fc = new JFileChooser();
			if (!(file == null)) {
				FileWriter writer = new FileWriter(file);
				writer.write(model.saveIt());
				writer.close();
			} else {
				saveAs();
			}
		} catch(IOException ex) {System.out.println("error in save()");}
	}

	public void load() {
		try {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(frame);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String s = "";
				while (reader.ready()) { 
					s = s + reader.readLine();
				}
				model.loadIt_Append(s);
				reader.close();
				listSRC.setListData(new Vector<String>(model.map.keySet()));
			} 
		} catch(IOException ex) {System.out.println("error in load()");}

		panel.initConnections();
		
	}

	public static void main (String[] args) {
		View02 view = new View02();
		view.init();
	}
//-----------------------------------------------------------------------------------------------------------------------------------
	public class SaveActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			save();
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------
	public class SaveAsActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			saveAs();
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------
	public class LoadActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			load();
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------
	public class TraceActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			try {
				//Note: Add code to make sure the right text is passed over
				//to model.tracert(ttl,wait,string).
				//Note: Look into, giving the retured set of keySet() directly to
				//listSRC, since it always reflets the keys contained in model.map
				model.trace(ttlT.getText(),waitT.getText(),targetT.getText());
				listSRC.setListData(new Vector<String>(model.map.keySet()));
			}
			catch(IOException ex){
				JOptionPane.showMessageDialog(null, "Error calling tracert!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
//-----------------------------------------------------------------------------------------------------------------------------------
	public class SrcListMouseListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if (!listSRC.isSelectionEmpty()) {
				Vector<String> vS = new Vector<String>(model.map.get(listSRC.getSelectedValue()).keySet());
				Vector<Float> vF = new Vector<Float>(model.map.get(listSRC.getSelectedValue()).values());
				Vector<String> v = new Vector<String>();
				for (int x = 0; x < vS.size() ; x++) {
					v.add(vS.get(x) + " : " + vF.get(x).intValue() + " hidden hops");
				}
				listDEST.setListData(v);
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
//-----------------------------------------------------------------------------------------------------------------------------------

}