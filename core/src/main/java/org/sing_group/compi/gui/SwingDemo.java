package org.sing_group.compi.gui;

import static java.util.Arrays.asList;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.xmlio.entities.Pipeline.fromFile;

import java.io.File;
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
import javax.swing.SpinnerNumberModel;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Task;
import org.xml.sax.SAXException;

public class SwingDemo {

  private static CompiApp compi;
  private static JTextField pipelineText;

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

    pipelineText = new JTextField(20);
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

    final JLabel skip = new JLabel("skip before task");
    skip.setBounds(10, 100, 150, 25);
    panel.add(skip);

    final JComboBox<String> skipComboBox = new JComboBox<String>();
    skipComboBox.setBounds(160, 100, 80, 25);
    skipComboBox.setEditable(false);
    skipComboBox.setEnabled(false);
    panel.add(skipComboBox);

    final JTextArea consoleTextArea = new JTextArea();

    final JScrollPane scrollArea =
      new JScrollPane(
        consoleTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
      );
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

        consoleTextArea.setText(null);
        skipComboBox.removeAllItems();
        skipComboBox.addItem("-");
        skipComboBox.setSelectedIndex(0);
        consoleTextArea.append("Task IDs\n");
        for (Task task: compi.getPipeline().getTasks()) {
          consoleTextArea.append("ID : " + task.getId() + "\n");
          skipComboBox.addItem(task.getId());
        }
        paramsButton.setEnabled(true);
        threadNumberText.setEnabled(true);
        skipComboBox.setEnabled(true);
      }
    });
    panel.add(pipelineButton);

    final JButton runButton = new JButton("Run");
    runButton.setBounds(150, 130, 80, 25);
    runButton.addActionListener(actionListener -> {
      new Thread(() -> {
        consoleTextArea.setText(null);
        try {
          compiExecution(
            (100 / compi.getPipeline().getTasks().size()),
            model.getNumber().intValue(), paramsText.getText(),
            skipComboBox.getSelectedItem().toString(), consoleTextArea, progressBar
          );
        } catch (JAXBException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }).start();

    });
    panel.add(runButton);

  }

  private static void compiExecution(
    int taskNumber, int threadNumber, String paramsFile,
    String skipTask, JTextArea consoleTextArea, JProgressBar progressBar
  ) throws JAXBException {
    if (skipTask.equals("-")) {
      skipTask = null;
    }
    if (paramsFile.isEmpty()) {
      paramsFile = null;
    }

    try {
      compi =
        new CompiApp(
          forPipeline(fromFile(new File(pipelineText.getText())))
            .whichRunsAMaximumOf(threadNumber)
            .whichResolvesVariablesFromFile(new File(paramsFile))
            .whichStartsFromTasks(skipTask != null ? asList(skipTask) : null)
            .build()
        );
      compi.addTaskExecutionHandler(new TaskExecutionHandler() {

        @Override
        public void taskStarted(Task task) {
          consoleTextArea.append(
            (System.currentTimeMillis() / 1000) + " - Task with id " + task.getId()
              + " started\n"
          );
          consoleTextArea.update(consoleTextArea.getGraphics());
        }

        @Override
        public void taskFinished(Task task) {
          if (task.isSkipped()) {
            if (task instanceof Foreach) {
              consoleTextArea.append("Task with id " + task.getId() + " skipped\n");
              final Task parent = ((ForeachIteration)task).getParentForeachTask();
              if (parent.isFinished()) {
                int percent = progressBar.getValue() + taskNumber;
                progressBar.setValue(percent);
                progressBar.update(progressBar.getGraphics());
              }
            } else {
              int percent = progressBar.getValue() + taskNumber;
              progressBar.setValue(percent);
              progressBar.update(progressBar.getGraphics());
            }
          } else {
            if (task instanceof Foreach) {
              consoleTextArea.append(
                (System.currentTimeMillis() / 1000) + " - SubTask with id "
                  + task.getId() + " finished\n"
              );
              final Task parent = ((ForeachIteration)task).getParentForeachTask();
              if (parent.isFinished()) {
                int percent = progressBar.getValue() + taskNumber;
                progressBar.setValue(percent);
                progressBar.update(progressBar.getGraphics());
              }
            } else {
              consoleTextArea.append(
                (System.currentTimeMillis() / 1000) + " - Task with id "
                  + task.getId() + " finished\n"
              );
              int percent = progressBar.getValue() + taskNumber;
              progressBar.setValue(percent);
              progressBar.update(progressBar.getGraphics());
            }
          }
          consoleTextArea.update(consoleTextArea.getGraphics());
        }

        @Override
        public void taskAborted(Task task, Exception e) {
          consoleTextArea.append(
            (System.currentTimeMillis() / 1000) + " - Task with id " + task.getId()
              + " aborted - Cause - " + e.getClass() + "\n"
          );
          consoleTextArea.update(consoleTextArea.getGraphics());
        }

        @Override
        public void taskIterationStarted(ForeachIteration iteration) {
          // TODO Auto-generated method stub
          
        }

        @Override
        public void taskIterationFinished(ForeachIteration iteration) {
          // TODO Auto-generated method stub
          
        }

        @Override
        public void taskIterationAborted(ForeachIteration iteration, Exception e) {
          // TODO Auto-generated method stub
          
        }

      });
      compi.run();
    } catch (
      InterruptedException | SAXException | IOException | ParserConfigurationException
      | IllegalArgumentException | PipelineValidationException e
    ) {
      consoleTextArea.append("--Error--\n");
      consoleTextArea.append("Type - " + e.getClass() + "\n");
      consoleTextArea.append("Message -  " + e.getMessage() + "\n");
    }
  }
}
