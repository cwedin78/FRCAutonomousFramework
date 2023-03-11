// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.io.IOException;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.libraries.MacroCANSparkMaxGroup;

public class ExampleSubsystem extends SubsystemBase {

  CANSparkMax testMotor = new CANSparkMax(1, MotorType.kBrushless);

  public MacroCANSparkMaxGroup macroTestMotor;

  /** Creates a new ExampleSubsystem. */
  public ExampleSubsystem() {
    try {
      macroTestMotor = new MacroCANSparkMaxGroup("TestMotor", testMotor);
    } catch (IOException e) {
      e.printStackTrace();
    }

    testMotor.getEncoder().setPosition(0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  public void drive(double speed) {
    testMotor.set(speed);
  }
}
