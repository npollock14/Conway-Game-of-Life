import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

//Github test
public class Driver extends JPanel
		implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	static Point mPos;
	int screenWidth = 1000;
	int screenHeight = 1000;
	boolean[] keys = new boolean[300];
	boolean[] keysToggled = new boolean[300];
	boolean[] mouse = new boolean[200];
	Camera cam = new Camera(0, 0, 1.0, screenWidth, screenHeight);
	Grid grid;
	double updateElapsed = 0;
	double updateTime = 100;
	double zoomTime = 10;
	int state = 0;
	StatusIcon si = new StatusIcon(0, new Point(20, screenHeight - 150), 100);
	boolean tap;
	FileManager f;

	// ============== end of settings ==================

	public void paint(Graphics g) {
		super.paintComponent(g);
		grid.draw(g);
		si.draw(g);
	}

	public void update() throws InterruptedException {

		if (mouse[1]) {
			try {
				grid.rects[(cam.toXMap(getMousePos().x) + 5) / 10][(cam.toYMap(getMousePos().y) + 4) / 10] = true;
				grid.alive
						.add(new Point((cam.toXMap(getMousePos().x) + 5) / 10, (cam.toYMap(getMousePos().y) + 4) / 10));
			} catch (Exception e) {

			}
		}
		if (mouse[3]) {
			try {
				grid.rects[(cam.toXMap(getMousePos().x) + 5) / 10][(cam.toYMap(getMousePos().y) + 4) / 10] = false;
				for (int i = 0; i < grid.alive.size(); i++) {
					if (grid.alive.get(i).x == (cam.toXMap(getMousePos().x) + 5) / 10
							&& grid.alive.get(i).y == (cam.toYMap(getMousePos().y) + 4) / 10) {
						grid.alive.remove(i);
						break;
					}

				}
			} catch (Exception e) {

			}

		}
		if (keys[67]) {
			grid.clear();
		}

		si.state = keysToggled[32] ? 1 : 0;

		if (keysToggled[32] && (System.currentTimeMillis() - updateElapsed >= updateTime
				|| (System.currentTimeMillis() - updateElapsed >= zoomTime && keys[90]))) {
			grid.update();
			updateElapsed = System.currentTimeMillis();
		}
		if (!keys[39])
			tap = true;
		if (keys[39] && si.state == 0 && tap) {
			grid.update();
			tap = false;
		}
		cam.update(keys, getMousePos());
		if (keys[79]) {
			String[] data = f.chooseOpen("Res\\DesignFiles", "life").split("x");
			if (data.length > 1) {
				grid.setData(data);
			}
			keys[79] = false;
		}

		if (keys[91] && keys[93]) {
			String result = "";
			for (int i = 0; i < grid.alive.size(); i++) {
				result += grid.alive.get(i).x + "," + grid.alive.get(i).y + "x";
			}
			f.chooseSave(result, ".life", "Res\\DesignFiles");
			keys[91] = false;
			keys[93] = false;
		}

	}

	private void init() {
		
		ArrayList<Point> init = new ArrayList<Point>();
		for (int i = 0; i < 10; i++) {
			init.add(new Point(100 + i, 100));
		}
		grid = new Grid(init, cam);
		f = new FileManager(this);
		cam.focus(new Point(500 * 10, 500 * 10));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ==================code above ===========================

	public Point getMousePos() {
		try {
			return new Point(this.getMousePosition().x, this.getMousePosition().y);
		} catch (Exception e) {
			return mPos;
		}
	}

	public int rBtw(int min, int max) {
		return ((int) (Math.random() * (max - min + 1) + min));
	}

	public Color rColor() {
		return new Color(rBtw(0, 255), rBtw(0, 255), rBtw(0, 255));
	}

	public double round(double number, int precision) { // rounds to precision
		number = number * Math.pow(10, precision);
		number = Math.round(number);
		number = number / Math.pow(10, precision);
		return number;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		try {
			update();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repaint();
	}

	public static void writeToFile(String path, String text) {
		// writes directly to file & will replace all previous text there
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
			bufferedWriter.write(text);
		} catch (IOException e) {
			System.out.println("Error: IO Exception");
		}
	}

	public static String readFile(String path) { // reads line by line
		String output = "";
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
			String line = bufferedReader.readLine();
			if (line != null) {
				output = line;

				for (line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
					output = output + "\n" + line;
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error: FILE NOT FOUND");
		} catch (IOException e) {
			System.out.println("Error: IO Exception");
		}
		return output;
	}

	public static void main(String[] arg) {
		@SuppressWarnings("unused")
		Driver d = new Driver();
	}

	public Driver() {
		JFrame f = new JFrame();
		f.setTitle("Conway's Game of Life");
		f.setSize(screenWidth, screenHeight);
		f.setBackground(Color.BLACK);
		f.setResizable(false);
		f.addKeyListener(this);
		f.addMouseMotionListener(this);
		f.addMouseWheelListener(this);
		f.addMouseListener(this);

		f.add(this);

		t = new Timer(15, this);
		t.start();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

		init();

	}

	Timer t;

	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;

	}

	@Override
	public void keyReleased(KeyEvent e) {

		keys[e.getKeyCode()] = false;

		if (keysToggled[e.getKeyCode()]) {
			keysToggled[e.getKeyCode()] = false;
		} else {
			keysToggled[e.getKeyCode()] = true;
		}

	}

	class Grid {
		boolean[][] rects = new boolean[1000][1000];
		int w = 10;
		ArrayList<Point> alive = new ArrayList<Point>();
		Camera cam;

		public Grid(ArrayList<Point> alive, Camera cam) {
			super();
			this.alive = alive;
			this.cam = cam;
			for (Point p : alive) {
				rects[p.x][p.y] = true;
			}
		}

		public void setData(String[] dataSplit) {
			ArrayList<Point> gridData = new ArrayList<Point>();
			try {
				for (int i = 0; i < dataSplit.length; i++) {
					gridData.add(new Point(Integer.parseInt(dataSplit[i].split(",")[0]),
							Integer.parseInt(dataSplit[i].split(",")[1])));
				}
			} catch (Exception e) {
				System.out.println("Parsing Error");
			}
			alive = gridData;
			rects = new boolean[1000][1000];
			for (Point p : alive) {
				rects[p.x][p.y] = true;
			}

		}

		public void clear() {

			for (int i = 0; i < rects.length; i++) {
				for (int j = 0; j < rects[i].length; j++) {
					rects[i][j] = false;
				}
			}
			alive.clear();

		}

		public void update() {
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			ArrayList<Point> toAddPs = new ArrayList<Point>();
			ArrayList<Point> toRemovePs = new ArrayList<Point>();
			int size = alive.size();
			for (int i = 0; i < size; i++) {
				Point a = alive.get(i);

				// get neighbor count
				int ns = getNeighbors(a);
				if (ns > 3 || ns < 2) {
					// dies
					if (!toRemove.contains(i)) {
						toRemove.add(i);
						toRemovePs.add(new Point(a.x, a.y));
					}

				}
				// check surroundings
				checkAround(a, toAddPs);

			}

			for (int i = 0; i < toRemove.size(); i++) {
				rects[toRemovePs.get(i).x][toRemovePs.get(i).y] = false;
				for (int k = 0; k < alive.size(); k++) {
					if (alive.get(k).x == toRemovePs.get(i).x && alive.get(k).y == toRemovePs.get(i).y) {
						alive.remove(k);
						// System.out.println("here");

					}
				}

			}

			toRemove.clear();
			toRemovePs.clear();
			outer: for (int i = 0; i < toAddPs.size(); i++) {
				for (Point p : alive) {
					if (p.x == toAddPs.get(i).x && p.y == toAddPs.get(i).y) {
						continue outer;
					}
				}

				try {
					rects[toAddPs.get(i).x][toAddPs.get(i).y] = true;
					alive.add(toAddPs.get(i));
				} catch (Exception e) {

				}
			}
			toAddPs.clear();
		}

		private void checkAround(Point a, ArrayList<Point> toAddPs) {
			for (int i = 0; i < 3; i++) {
				for (int k = 0; k < 3; k++) {
					if (!(k == 1 && i == 1)) {
						if (getNeighbors(new Point(a.x - 1 + k, a.y - 1 + i)) == 3) {
							toAddPs.add(new Point(a.x - 1 + k, a.y - 1 + i));
						}

					}
				}
			}

		}

		public int getNeighbors(Point a) {
			int ns = 0;

			try {
				ns += rects[a.x - 1][a.y] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x + 1][a.y] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x][a.y + 1] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x][a.y - 1] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x + 1][a.y + 1] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x - 1][a.y - 1] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x - 1][a.y + 1] ? 1 : 0;
			} catch (Exception e) {

			}
			try {
				ns += rects[a.x + 1][a.y - 1] ? 1 : 0;
			} catch (Exception e) {

			}
			return ns;
		}

		public void draw(Graphics g) {
			for (Point a : alive) {
				g.fillRect(cam.toXScreen(a.x * (w) - w / 2), cam.toYScreen((a.y * (w)) - (w / 2)),
						(int) (w * cam.scale), (int) (w * cam.scale));
			}
			for (int i = 0; i <= rects.length + 1; i++) {
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(cam.toXScreen(i * 10 - w / 2), cam.toYScreen(0 - w / 2), cam.toXScreen(i * 10 - w / 2),
						cam.toYScreen(10000 + 5));
				g.drawLine(cam.toXScreen(0 - w / 2), cam.toYScreen(i * 10 - w / 2), cam.toXScreen(10000 - w / 2),
						cam.toYScreen(i * 10 - w / 2));
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		if (e.getWheelRotation() < 0) {
			cam.changeScale(.1f);
		} else {
			cam.changeScale(-.1f);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouse[e.getButton()] = true;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouse[e.getButton()] = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse[e.getButton()] = true;

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	public static Point getmPos() {
		return mPos;
	}

}

class Point {
	int x, y;

	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double distanceTo(Point p2) {
		return Math.sqrt((this.x - p2.x) * (this.x - p2.x) + (this.y - p2.y) * (this.y - p2.y));
	}

	public double angleTo(Point p2) {
		try {
			return Math.atan2(this.y - p2.y, this.x - p2.x);
		} catch (Exception e) {

		}
		return 0;
	}

	public void print() {
		System.out.println("(" + x + ", " + y + ")");
	}
}

class Camera {
	int xOff, yOff, screenW, screenH;
	double scale;
	Point center;
	float scaleNotches = 0;
	int moveSpeed = 10;

	public Camera(int xOff, int yOff, double scale, int screenW, int screenH) {
		super();
		this.xOff = xOff;
		this.yOff = yOff;
		this.scale = scale;
		this.screenW = screenW;
		this.screenH = screenH;
		center = new Point(screenW / 2, screenH / 2);
	}

	public void update(boolean[] keys, Point mousePos) {
		xOff += keys[65] ? moveSpeed / scale : 0;
		xOff -= keys[68] ? moveSpeed / scale : 0;
		yOff += keys[87] ? moveSpeed / scale : 0;
		yOff -= keys[83] ? moveSpeed / scale : 0;

	}

	public void focus(Point p) {
		// p = map coordinates
		// place point p in center of screen & determine how displaced that was
		xOff = screenW / 2 - p.x;
		yOff = screenH / 2 - p.y;

	}

	public void changeScale(float notches) {
		scaleNotches += notches;
		scale = Math.pow(2, scaleNotches);
	}

	public int toXScreen(int x) {
		int dx = (int) ((x + xOff - center.x) * scale);
		return (center.x + dx);

	}

	public int toYScreen(int y) {
		int dy = (int) ((y + yOff - center.y) * scale);
		return (center.y + dy);

	}

	public int toXMap(int x) {
		return (int) ((x - center.x) / scale) + center.x - xOff;

	}

	public int toYMap(int y) {
		return (int) ((y - center.y) / scale) + center.y - yOff;

	}

}

class StatusIcon {
	int state;
	Point pos;
	int w;

	public StatusIcon(int state, Point pos, int w) {
		super();
		this.state = state;
		this.pos = pos;
		this.w = w;
	}

	public void draw(Graphics g) {
		g.setColor(Color.BLACK);
		if (state == 0) { // paused
			g.fillRect(pos.x, pos.y, w / 3, w);
			g.fillRect(pos.x + w - w / 3, pos.y, w / 3, w);
		}
		if (state == 1) { // play
			int[] xs = { pos.x, pos.x, pos.x + w };
			int[] ys = { pos.y, pos.y + w, pos.y + w / 2 };
			g.fillPolygon(xs, ys, 3);
		}
	}

}

class FileManager {
	JFileChooser fc = new JFileChooser();
	String targetExtention;
	Component c;

	public FileManager(Component c) {
		this.c = c;
	}

	public void chooseSave(String text, String extention) {
		fc.setCurrentDirectory(new File(""));
		int returnVal = fc.showSaveDialog(c);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String path = fc.getSelectedFile().getPath() + (extention.length() > 0 ? extention : "");
				System.out.println("Saving as: " + path);
				writeToFile(path, text);
				System.out.println("Saved");
			} catch (Exception e) {
				System.out.println("File Error");
			}

		} else {
			System.out.println("Save command cancelled by user.");
		}
	}

	public void chooseSave(String text, String extention, String startPath) {
		fc.setCurrentDirectory(new File(startPath));
		int returnVal = fc.showSaveDialog(c);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String path = fc.getSelectedFile().getPath() + (extention.length() > 0 ? extention : "");
				System.out.println("Saving as: " + path);
				writeToFile(path, text);
				System.out.println("Saved");
			} catch (Exception e) {
				System.out.println("File Error");
			}

		} else {
			System.out.println("Save command cancelled by user.");
		}
	}

	public String chooseOpen(String extention) {
		fc.setCurrentDirectory(new File(""));
		int returnVal = fc.showOpenDialog(c);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String path = fc.getSelectedFile().getPath();
				System.out.println("Opening: " + path + ".");
				String data = readFile(path);
				return data;
			} catch (Exception e) {
				return "";
			}

		} else {
			System.out.println("Open command cancelled by user.");
			return "";
		}

	}

	public String chooseOpen(String startPath, String extention) {
		fc.setCurrentDirectory(new File(startPath));
		fc.setFileFilter(new FileNameExtensionFilter("", extention.length() > 0 ? extention : "*"));
		int returnVal = fc.showOpenDialog(c);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				String path = fc.getSelectedFile().getPath();
				System.out.println("Opening: " + path + ".");
				String data = readFile(path);
				return data;
			} catch (Exception e) {
				return "";
			}

		} else {
			System.out.println("Open command cancelled by user.");
			return "";
		}

	}

	public static void writeToFile(String path, String text) {
		// writes directly to file & will replace all previous text there
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
			bufferedWriter.write(text);
		} catch (IOException e) {
			System.out.println("Error: IO Exception");
		}
	}

	public static String readFile(String path) { // reads line by line
		String output = "";
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
			String line = bufferedReader.readLine();
			if (line != null) {
				output = line;

				for (line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
					output = output + "\n" + line;
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error: FILE NOT FOUND");
		} catch (IOException e) {
			System.out.println("Error: IO Exception");
		}
		return output;
	}

}