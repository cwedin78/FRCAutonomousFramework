// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import java.io.FileNotFoundException;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.libraries.AutoFramework.AutoCommandBase;
import frc.robot.subsystems.ExampleSubsystem;

public class ReadMacro extends AutoCommandBase {
  ExampleSubsystem subsystem;
  /** Creates a new ReadMacro. */
  public ReadMacro(ExampleSubsystem exampleSubsystem) {
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(exampleSubsystem);
    subsystem = exampleSubsystem;
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    try {
      subsystem.macroTestMotor.beginRead();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    subsystem.macroTestMotor.readFrame();
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    subsystem.macroTestMotor.endRead();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
