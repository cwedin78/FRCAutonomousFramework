// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import java.io.IOException;
import java.util.function.DoubleSupplier;

import frc.robot.libraries.AutoFramework.AutoCommandBase;
import frc.robot.subsystems.ExampleSubsystem;

public class RecordDrive extends AutoCommandBase {
  ExampleSubsystem m_ExampleSubsysten;

  DoubleSupplier speed;
  /** Creates a new test. */
  public RecordDrive(ExampleSubsystem exampleSubsystem, DoubleSupplier driveSpeed) {
    m_ExampleSubsysten = exampleSubsystem;
    addRequirements(exampleSubsystem);
    speed = driveSpeed;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    try {
      m_ExampleSubsysten.macroTestMotor.beginRecord();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    m_ExampleSubsysten.drive(speed.getAsDouble());

    try {
      m_ExampleSubsysten.macroTestMotor.recordFrame();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_ExampleSubsysten.drive(0);

    try {
      m_ExampleSubsysten.macroTestMotor.endRecord();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
