package view;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Concise animated demo to Branch Predictor
 *
 */
public class Main {
	JFrame frame;
	
	int unitWidth = 30;
	int unitHeight = 30;

	public static void main(String args[]) {
		Main obj = new Main();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				obj.initialize();
			}
		};

		SwingUtilities.invokeLater(runnable);
	}

	public void initialize() {
		frame = new JFrame("Simple Animation Demo of Branch Predictor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane tabbedPane = new JTabbedPane();

		AnimationPanel tab1= new WithoutBPAnimationPanel();
		tab1.createUI();
		tabbedPane.addTab("Without Branch Predictor",null, tab1, "Without Branch Predictor");

		AnimationPanel tab2 = new BPNotTakeAnimationPanel();
		tab2.createUI();
		tabbedPane.addTab("BP Not Take", null, tab2, "Branch Predictor Not Take"); 
		
		AnimationPanel tab3 = new BPTakeAnimationPanel();
		tab3.createUI();
		tabbedPane.addTab("BP Take", null, tab3, "Branch Predictor Take");

		frame.add(tabbedPane, BorderLayout.CENTER);
		frame.setResizable(false);
		frame.setLocation(100, 100);
		frame.pack();
		frame.setVisible(true);
		
		tabbedPane.addChangeListener(new TabChangeListener(tab1, tab2, tab3));
	}
	
	/**
	 *  Called by each CPU clock cycle 
	 *
	 */
	class ClockCycleTask extends TimerTask{

		private AnimationPanel panel;
		private boolean launchedEver = false;
		long delay = 1000;
		long period = 500;

		public ClockCycleTask(AnimationPanel panel){
			this.panel = panel;
		}

		@Override
		public void run() {
			panel.pulse(period);
		}
		
		public void focus(){
			panel.reset();
			if(!launchedEver){
				new Timer().schedule(this, delay, period);
				launchedEver = true;
			}
		}
		
		public void leave(){
			panel.cancel();
		}
	}

	class TabChangeListener implements ChangeListener{
		
		AnimationPanel tab1;
		AnimationPanel tab2;
		AnimationPanel tab3;
		
		ClockCycleTask clockCycleTask1 = null;
		ClockCycleTask clockCycleTask2 = null;
		ClockCycleTask clockCycleTask3 = null;
		
		public TabChangeListener(AnimationPanel tab1, AnimationPanel tab2, AnimationPanel tab3) {
			this.tab1 = tab1;
			this.tab2 = tab2;
			this.tab3 = tab3;
			
			this.clockCycleTask1 = new ClockCycleTask(tab1);
			this.clockCycleTask2 = new ClockCycleTask(tab2);
			this.clockCycleTask3 = new ClockCycleTask(tab3);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
			int index = sourceTabbedPane.getSelectedIndex();

			if(index == 0){
				clockCycleTask1.focus();
				clockCycleTask2.leave();
				clockCycleTask3.leave();
			}else if(index == 1){
				clockCycleTask1.leave();
				clockCycleTask2.focus();
				clockCycleTask3.leave();
			}else if(index == 2){
				clockCycleTask1.leave();
				clockCycleTask2.leave();
				clockCycleTask3.focus();
			}
		}
	}

	/**
	 * Show how several instructions are executed in pipeline 
	 *
	 */
	abstract class AnimationPanel extends JPanel{
		static final long serialVersionUID = 1L;
		GridBagConstraints c;
		
		int totalRow = 10;
		int totalColumn = 15;
		int rowStart = 1;
		int columnStart = 5;
		int pipelineRow = 4;

		int runtimeRow_normal_instruction2 = rowStart;
		int runtimeRow_normal_instruction1 = rowStart + 1;
		int runtimeRow_conditional_jump = rowStart + 2;
		int runtimeColumn = columnStart;
		int executeRow = rowStart + 5;
		int bottomRow = rowStart + 9;
		
		public void createUI(){
			this.setLayout(new GridBagLayout());
			this.setBackground(Color.black);
			c = new GridBagConstraints();
			
			for(int i = 0; i < totalRow + 1; i++){
				for(int j = 0; j < totalColumn; j++){
					c.gridx = j;
					c.gridy = i;
					this.add(new PlaceHolder(), c);
				}
			}
			
			JPanel clockCycle = new StringPanel(3,1,"Clock Cycle");
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 3;
			this.add(clockCycle, c);

			c.gridwidth = 2;
			JPanel waitingPanel = new StringPanel(2,1,"Waiting");
			c.gridy = 2;
			this.add(waitingPanel, c);
			c.gridwidth = 4;
			
			c.gridy = 4;
			JPanel fetchPanel = new StringPanel(4,1, "Stage 1: Fetch");
			this.add(fetchPanel, c);
			c.gridy = 5;
			JPanel decodePanel = new StringPanel(4,1, "Stage 2: Decode");
			this.add(decodePanel, c);
			c.gridy = 6;
			JPanel executePanel = new StringPanel(4,1, "Stage 3: Execute");
			this.add(executePanel, c);
			c.gridy = 7;
			JPanel writeBackPanel = new StringPanel(4,1, "Stage 4: WB");
			this.add(writeBackPanel, c);
			c.gridwidth = 2;

			JPanel completedPanel = new StringPanel(2,1, "Completed");
			c.gridy = 9;
			this.add(completedPanel, c);

			c.gridwidth = 1;

			for (int i = columnStart; i < totalColumn; i++) {
				c.gridx = i;
				c.gridy = 0;
				this.add(new NumberPanel(i - columnStart), c);
			}
			
			int stageNumber = 4;

			for(int i = pipelineRow; i < pipelineRow + stageNumber; i++){
				for(int j = columnStart; j < totalColumn; j++){
					c.gridx = j;
					c.gridy = i;
					this.add(new Stage(), c);
				}
			}
		}
		
		public void pulse(long period){
			if(isFinish()){
				return;
			}
			
			Instruction conditional_jump = new Instruction(new Color(15, 244, 244));
			Instruction normal_take1 = new Instruction(new Color(242, 236, 20));
			Instruction normal_take2 = new Instruction(new Color(255,69,69));
			
			c.gridx = runtimeColumn;
			c.gridy = runtimeRow_normal_instruction2;
			this.add(normal_take2, c);
			c.gridy = runtimeRow_normal_instruction1;
			this.add(normal_take1, c);
			c.gridy = runtimeRow_conditional_jump; 
			this.add(conditional_jump, c);
			
			this.revalidate();

			act();
			
			try{
				Thread.sleep(period - 100);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			this.remove(normal_take1);
			this.remove(normal_take2);
			this.remove(conditional_jump);
			this.revalidate();
			this.repaint();
		}
		
		public void reset(){
			runtimeRow_normal_instruction2 = rowStart;
			runtimeRow_normal_instruction1 = rowStart + 1;
			runtimeRow_conditional_jump = rowStart + 2;
			runtimeColumn = columnStart;
		}
		
		public boolean isFinish(){
			return runtimeColumn == totalColumn;
		}
		
		public void cancel(){
			runtimeColumn = totalColumn;
		}
		
		public abstract void act();
	}

	/**
	 * The instructions executed without Branch Predictor 
	 *
	 */
	class WithoutBPAnimationPanel extends AnimationPanel{
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void act(){
			runtimeColumn++;
			if(runtimeRow_conditional_jump < bottomRow){
				runtimeRow_conditional_jump++;
			}
			if(runtimeRow_conditional_jump > executeRow || runtimeRow_normal_instruction1 < pipelineRow - 1){
				runtimeRow_normal_instruction2++;
				runtimeRow_normal_instruction1++;
			}
		}
	}
	
	/**
	 * The instructions executed with Branch Predictor and the conditional jump is not taken
	 *
	 */
	class BPNotTakeAnimationPanel extends AnimationPanel{
		
		private static final long serialVersionUID = 1L;
		public BPNotTakeAnimationPanel(){
			super();
			totalColumn = totalColumn - 2;
		}
		
		@Override
		public void act(){
			runtimeColumn++;
			if(runtimeRow_conditional_jump < bottomRow){
				runtimeRow_conditional_jump++;
			}

			runtimeRow_normal_instruction2++;
			runtimeRow_normal_instruction1++;

		}
	}
	
	/**
	 * 
	 * The instructions executed with Branch Predictor and the conditional jump is taken
	 * 
	 */
	class BPTakeAnimationPanel extends AnimationPanel{
		
		private static final long serialVersionUID = 1L;
		private boolean anotherBranch = true;

		@Override
		public void act(){
			runtimeColumn++;
			if(runtimeRow_conditional_jump < bottomRow){
				runtimeRow_conditional_jump++;
			}


			runtimeRow_normal_instruction2++;
			runtimeRow_normal_instruction1++;
			
			if(runtimeRow_conditional_jump > executeRow){
				if(anotherBranch){
					runtimeRow_normal_instruction2 = rowStart + 2;
					runtimeRow_normal_instruction1 = rowStart + 3;
					anotherBranch = false;
				}
			}
		}
		
		@Override
		public void reset(){
			super.reset();
			anotherBranch = true;
		}
	}

	// Below panels are all UI resources related
	
	class Instruction extends JPanel {
		private static final long serialVersionUID = 1L;

		Color color;

		public Instruction(Color color) {
			this.color = color;
			this.setBackground(color);
			this.setBorder(BorderFactory.createLineBorder(Color.gray));
			this.setPreferredSize(new Dimension(unitWidth, unitHeight));
		}
	}

	class NumberPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		int number;

		public NumberPanel(int number) {
			this.number = number;
			this.setPreferredSize(new Dimension(unitWidth, unitHeight));
			this.setBackground(Color.black);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.white);
			g.drawString(Integer.toString(number), 10, 20);
		}
	}
	
	public class PlaceHolder extends JPanel{
		private static final long serialVersionUID = 1L;

		public PlaceHolder(){
			this.setPreferredSize(new Dimension(unitWidth, unitHeight));
			this.setBackground(new Color(255,255,255,16));
		}
	}
	
	public class StringPanel extends JPanel{

		private static final long serialVersionUID = 1L;
		private String content;
		public StringPanel(int widthAmount, int heightAmount, String content){
			this.content = content;
			this.setPreferredSize(new Dimension(widthAmount * unitWidth, heightAmount * unitHeight));
		}
		
		@Override
		public void paintComponent(Graphics g){
			g.setColor(Color.white);
			g.drawString(content, 0, 20);
		}
	}
	
	public class Stage extends JPanel{

		private static final long serialVersionUID = 1L;
		public Stage(){
			this.setPreferredSize(new Dimension(unitWidth, unitHeight));
			this.setBorder(BorderFactory.createLineBorder(Color.black));
			this.setBackground(Color.lightGray);
		}
	}
	
}
