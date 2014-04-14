package com.pi.robot.demo;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.pi.robot.robot.MotorState;
import com.pi.robot.robot.RobotStateManager;

public class Configurator {
	private JFrame frame;

	private Map<JComponent, Field> fieldMap = new HashMap<JComponent, Field>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Configurator window = new Configurator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Configurator() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(25, 2, 0, 0));

		// Dynamically generate contents
		try {
			Field[] fields = Configuration.class.getFields();
			for (Field f : fields) {
				Class<?> type = f.getType();
				if (type.getSimpleName().equals("boolean")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JRadioButton btn = new JRadioButton();
					btn.setSelected(f.getBoolean(null));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				} else if (type.getSimpleName().equals("float")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JTextField btn = new JTextField();
					btn.setText(String.valueOf(f.getFloat(null)));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				} else if (type.getSimpleName().equals("int")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JTextField btn = new JTextField();
					btn.setText(String.valueOf(f.getInt(null)));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				} else if (type.getSimpleName().equals("String")) {
					frame.getContentPane().add(new JLabel(f.getName()));
					JTextField btn = new JTextField();
					btn.setText(String.valueOf(f.get(null)));
					fieldMap.put(btn, f);
					frame.getContentPane().add(btn);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JButton submit = new JButton("Launch");
		submit.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				store();
				RobotStateManager.robot.driveLeftState.setState(MotorState
						.decode(RobotStateManager.robot.driveLeftState.getState()
								.ordinal() + 1));
				RobotStateManager.robot.driveRightState.setState(MotorState
						.decode(RobotStateManager.robot.driveRightState.getState()
								.ordinal() + 1));
			}
		});
		frame.getContentPane().add(submit);
	}

	public void store() {
		for (Entry<JComponent, Field> et : fieldMap.entrySet()) {
			try {
				if (et.getValue().getType().getSimpleName().equals("float")) {
					et.getValue()
							.setFloat(
									null,
									Float.valueOf(((JTextField) et.getKey())
											.getText()));
				} else if (et.getValue().getType().getSimpleName()
						.equals("int")) {
					et.getValue().setInt(
							null,
							Integer.valueOf(((JTextField) et.getKey())
									.getText()));
				} else if (et.getValue().getType().getSimpleName()
						.equals("String")) {
					et.getValue().set(null,
							((JTextField) et.getKey()).getText());
				} else if (et.getValue().getType().getSimpleName()
						.equals("boolean")) {
					et.getValue().setBoolean(null,
							((JRadioButton) et.getKey()).isSelected());
				}
			} catch (Exception e) {
			}
		}
	}
}
