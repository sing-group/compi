package org.sing_group.compi.gui;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.ProgramExecutionHandler;
import org.sing_group.compi.xmlio.entities.Program;
import org.xml.sax.SAXException;

public class SwingDemo {

	private static CompiApp compi;

	public static void main(String[] args) {
		final JFrame frame = new JFrame("Compi");
		frame.setSize(400, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JPanel panel = new JPanel();
		frame.add(panel);
		placeComponents(panel);

		frame.setVisible(true);
	}

	private static void placeComponents(JPanel panel) {

		panel.setLayout(null);

		final JLabel pipeline = new JLabel("pipeline");
		pipeline.setBounds(10, 10, 80, 25);
		panel.add(pipeline);

		final JTextField pipelineText = new JTextField(20);
		pipelineText.setBounds(100, 10, 160, 25);
		pipelineText.setEditable(false);
		panel.add(pipelineText);

		final JLabel params = new JLabel("params");
		params.setBounds(10, 40, 80, 25);
		panel.add(params);

		final JTextField paramsText = new JTextField(20);
		paramsText.setBounds(100, 40, 160, 25);
		paramsText.setEditable(false);
		panel.add(paramsText);

		final JButton paramsButton = new JButton("get file");
		paramsButton.setBounds(270, 40, 100, 25);
		paramsButton.addActionListener(actionListener -> {
			final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				paramsText.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		paramsButton.setEnabled(false);
		panel.add(paramsButton);

		final JLabel threadNumber = new JLabel("thread number");
		threadNumber.setBounds(10, 70, 125, 25);
		panel.add(threadNumber);

		final SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 10, 1);
		final JSpinner threadNumberText = new JSpinner(model);
		threadNumberText.setBounds(130, 70, 80, 25);
		threadNumberText.setEnabled(false);
		panel.add(threadNumberText);

		final JLabel skip = new JLabel("skip before program");
		skip.setBounds(10, 100, 150, 25);
		panel.add(skip);

		final JComboBox<String> skipComboBox = new JComboBox<String>();
		skipComboBox.setBounds(160, 100, 80, 25);
		skipComboBox.setEditable(false);
		skipComboBox.setEnabled(false);
		panel.add(skipComboBox);

		final JTextArea consoleTextArea = new JTextArea();

		final JScrollPane scrollArea = new JScrollPane(consoleTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollArea.setBounds(10, 160, 380, 250);
		panel.add(scrollArea);

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(10, 420, 380, 25);
		progressBar.setStringPainted(true);
		panel.add(progressBar);

		final JButton pipelineButton = new JButton("get file");
		pipelineButton.setBounds(270, 10, 100, 25);
		pipelineButton.addActionListener(actionListener -> {
			final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				pipelineText.setText(chooser.getSelectedFile().getAbsolutePath());
				try {
					compi = new CompiApp(pipelineText.getText());
					consoleTextArea.setText(null);
					skipComboBox.removeAllItems();
					skipComboBox.addItem("-");
					skipComboBox.setSelectedIndex(0);
					consoleTextArea.append("Programs IDs\n");
					for (String programID : compi.getProgramManager().getProgramsLeft()) {
						consoleTextArea.append("ID : " + programID + "\n");
						skipComboBox.addItem(programID);
					}
					paramsButton.setEnabled(true);
					threadNumberText.setEnabled(true);
					skipComboBox.setEnabled(true);
				} catch (SAXException | IOException | JAXBException e) {
					consoleTextArea.append("--Error--\n");
					consoleTextArea.append("Type - " + e.getClass() + "\n");
					consoleTextArea.append("Message -  " + e.getMessage() + "\n");
				}
			}
		});
		panel.add(pipelineButton);

		final JButton runButton = new JButton("Run");
		runButton.setBounds(150, 130, 80, 25);
		runButton.addActionListener(actionListener -> {
			new Thread(() -> {
				consoleTextArea.setText(null);
				compiExecution(compi, (100 / compi.getProgramManager().getDAG().size()),
						model.getNumber().intValue(), paramsText.getText(),
						skipComboBox.getSelectedItem().toString(), consoleTextArea, progressBar);
			}).start();

		});
		panel.add(runButton);

	}

	private static void compiExecution(CompiApp compi, int programNumber, int threadNumber, String paramsFile,
			String skipProgram, JTextArea consoleTextArea, JProgressBar progressBar) {
		if (skipProgram.equals("-")) {
			skipProgram = null;
		}
		if (paramsFile.isEmpty()) {
			paramsFile = null;
		}

		try {
			compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

				@Override
				public void programStarted(Program program) {
					consoleTextArea.append((System.currentTimeMillis() / 1000) + " - Program with id " + program.getId()
							+ " started\n");
					consoleTextArea.update(consoleTextArea.getGraphics());
				}

				@Override
				public void programFinished(Program program) {
					if (program.isSkipped()) {
						if (program.getForeach() != null) {
							consoleTextArea.append("Program with id " + program.getId() + " skipped\n");
							final Program parent = compi.getParentProgram().get(program);
							if (parent.isFinished()) {
								int percent = progressBar.getValue() + programNumber;
								progressBar.setValue(percent);
								progressBar.update(progressBar.getGraphics());
							}
						} else {
							int percent = progressBar.getValue() + programNumber;
							progressBar.setValue(percent);
							progressBar.update(progressBar.getGraphics());
						}
					} else {
						if (program.getForeach() != null) {
							consoleTextArea.append((System.currentTimeMillis() / 1000) + " - SubProgram with id "
									+ program.getId() + " finished\n");
							final Program parent = compi.getParentProgram().get(program);
							if (parent.isFinished()) {
								int percent = progressBar.getValue() + programNumber;
								progressBar.setValue(percent);
								progressBar.update(progressBar.getGraphics());
							}
						} else {
							consoleTextArea.append((System.currentTimeMillis() / 1000) + " - Program with id "
									+ program.getId() + " finished\n");
							int percent = progressBar.getValue() + programNumber;
							progressBar.setValue(percent);
							progressBar.update(progressBar.getGraphics());
						}
					}
					consoleTextArea.update(consoleTextArea.getGraphics());
				}

				@Override
				public void programAborted(Program program, Exception e) {
					consoleTextArea.append((System.currentTimeMillis() / 1000) + " - Program with id " + program.getId()
							+ " aborted - Cause - " + e.getClass() + "\n");
					consoleTextArea.update(consoleTextArea.getGraphics());
				}

			});
			compi.run(threadNumber, paramsFile, skipProgram, null);
		} catch (InterruptedException | SAXException | IOException | ParserConfigurationException
				| IllegalArgumentException e) {
			consoleTextArea.append("--Error--\n");
			consoleTextArea.append("Type - " + e.getClass() + "\n");
			consoleTextArea.append("Message -  " + e.getMessage() + "\n");
		}
	}
}
