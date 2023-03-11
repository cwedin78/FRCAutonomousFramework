package frc.robot.libraries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

public class MacroCANSparkMaxGroup {

    final static double defaultKP = 0.03, defaultKI = 0, defaultKD = 0;

    private final ShuffleboardTab kShuffleboardTab;
    private GenericEntry[] kPositionArr;

    /**
     * The path in which the macro item will store and read macro files
     */
    public final String dataPath = Filesystem.getDeployDirectory().getPath();

    /**
     * File name specified by constructor.
     */
    public final String name;

    /**
     * The file specified by dataPath that the macro
     * will attempt to read from and write to.
     */
    public final File mapFile;

    // Not final to allow reinstancing.
    FileWriter writer;
    Scanner scanner;

    /**
     * CANSparkMax defined by constructor. This is final.
     */
    public final CANSparkMax[] canSparkMaxArr;

    final Timer RecordTimer = new Timer(), ReadTimer = new Timer();

    // To allow timer to wait until it has passed this value before
    // reading next macro line.
    double lastReadTimer = 0;

    /**
     * Embedded PID controller for driving the motor to the 
     * position stated by the macro at each frame. The faster the PID
     * loop drives the motor to its goal position, the higher the
     * accuracy.
     * 
     * @see {@link frc.robot.libraries.PIDController}
     */
    final PIDController[] driveControllerArr;

    /**
     * Creates an instance of a class extending the function of any number of
     * {@link com.revrobotics.CANSparkMax} to allow for positional macro use.
     * 
     * @param MotorArr CANSparkMax instances to add to macro group
     * @param Name for the written/read file
     * 
     * @apiNote MacroCANSparkMaxGroup with an equal Name value will overwrite
     * eachother during read/write operations. Avoid creating instances with
     * equal name values.
     * 
     * @throws IOException when file location is innaccessable.
     * 
     * TODO * Test multiple CANSparkMax items in macro, and test 
     * TODO * drive accuracy for tank and swerve.
     */
    public MacroCANSparkMaxGroup(String Name, CANSparkMax... MotorArr) throws IOException {
        canSparkMaxArr = MotorArr;

        name = Name;

        mapFile = new File( dataPath + "\\" + name + ".txt");

        // Initalize PIDController for each motor
        driveControllerArr = new PIDController[MotorArr.length];

        for (int i = 0; i < MotorArr.length; i++) {
            driveControllerArr[i] = new PIDController(defaultKP, defaultKI, defaultKD, 0);
        }

        // Setup shuffleboard catagories
        kShuffleboardTab = Shuffleboard.getTab("MacroGroup" + name);

        kPositionArr = new GenericEntry[MotorArr.length];
        // Add entries to chuffleboard for each motor
        for (int i = 0; i < MotorArr.length; i++) {
            kPositionArr[i] = kShuffleboardTab.addPersistent("Position " + i, 0).getEntry();  
        }
    }

    /**
     * Sets the P, I, and D values of the embedded {@link PIDController}
     * @param kP Proportion of the error
     * @param kI Proportion of the integral of the error
     * @param kD Proportion of the derivative of the error
     * 
     * @return this for chaining
     */
    public MacroCANSparkMaxGroup setPID(double kP, double kI, double kD) {

        for (PIDController driveController : driveControllerArr) {
            driveController.kP = kP;
            driveController.kI = kI;
            driveController.kD = kD;
        }

        return this;
    }

    /**
     * Starts the record timer and institates the 
     * writer, and clears the file at the defined path. Note 
     * that this function can be ran in a seperate section of 
     * code than what is driving the motor.
     * 
     * @apiNote {@link CANSparkMax} {@link RelativeEncoder} position
     * is not reset when recording begins, to allow the user to do this
     * at the proper time. Note that resetting the encoder takes a short
     * amount of time, and if done shortly before recording begins, the first
     * few frames of the macro will contain encoder values before its reset 
     * 
     * @see {@link com.revrobotics.RelativeEncoder.setPosition(double position)}
     * 
     * @throws IOException to allow user to handle how the macro
     * process should be handled if this error occurs
     */
    public void beginRecord() throws IOException {

        // for (CANSparkMax canSparkMax : canSparkMaxArr) {
        //     canSparkMax.getEncoder().setPosition(0); // Let the user do this at the right time   
        // }

        RecordTimer.restart();
        RecordTimer.start();

        writer = new FileWriter(mapFile, true);

        // Clear the file
        PrintWriter writer = new PrintWriter(mapFile);
        writer.print(""); // Write empty
        writer.close();
    }

    /**
     * Record a single frame -- write the time, and motor position,
     * to the macro file. Note that this function can be ran in a 
     * seperate section of code than what is driving the motor.
     * 
     * @apiNote Lines are written in the format;
     * {@code String toWrite = RecordTimer.get() + "," + encoder.getPosition() + "\n";}
     * 
     * @param TimeSeconds Current macro time
     * 
     * @throws IOException to allow user to handle how the macro
     * process should be handled if this error occurs
     */
    public void recordFrame() throws IOException {

        // Note this method to write the macros, this same format will be read.
        String toWrite = "" + RecordTimer.get();

        for (CANSparkMax canSparkMax : canSparkMaxArr) {
            toWrite += ("," + canSparkMax.getEncoder().getPosition());
        }

        toWrite += "\n";

        writer.append(toWrite);
    }

    /**
     * Closes the file writer, and resets the timer. Note 
     * that this function can be ran in a seperate section of 
     * code than what is driving the motor.
     * 
     * @throws IOException to allow user to handle how the macro
     * process should be handled if this error occurs
     */
    public void endRecord() throws IOException {

        writer.close();

        // Reset the timer
        RecordTimer.stop();
        RecordTimer.reset();

        // encoder.setPosition(0); // fuck you batman
    }

    /**
     * Starts the macro read timer and initializes the scanner.
     * 
     * @apiNote {@link CANSparkMax} {@link RelativeEncoder} position
     * is not reset when reading begins, to allow the user to do this
     * at the proper time. Note that resetting the encoder takes a short
     * amount of time, and if done shortly before reading begins, the macro
     * may attempt to drive the motor to the starting macro value for the
     * first few frames.
     * 
     * @throws FileNotFoundException if the path attempted to be
     * read is inaccessable by the code.
     */
    public void beginRead() throws FileNotFoundException {
        ReadTimer.restart();
        ReadTimer.start();

        // Create a new scanner instance to override the last
        scanner = new Scanner(mapFile);

        scanner.useDelimiter(",|\\n");

        // Grab the start time of the macro
        if (scanner.hasNext()) {
            lastReadTimer = scanner.nextDouble();
        }
    }
    
    /**
     * Reads a single frame from the macro if current time has passed 
     * the LastReadTimer value. The canSparkMax will be set to
     * the output of the driveController.
     */
    public void readFrame() {

        for (int i = 0; i < canSparkMaxArr.length; i++) {
            driveControllerArr[i].setInput(canSparkMaxArr[i].getEncoder().getPosition());   
        }

        // Only progress to read the position if the time has passed the
        // time defined within the macro.
        if (ReadTimer.get() > lastReadTimer) {

            for (PIDController driveController : driveControllerArr) {
                if (scanner.hasNext()) {
                    // Read the macro value
                    driveController.setTarget(scanner.nextDouble());
                }   
            }

            if (scanner.hasNext()) {
                //Read the next timer value
                lastReadTimer = scanner.nextDouble();
            }
        }

        // set the motor
        for (int i = 0; i < canSparkMaxArr.length; i++) {
            canSparkMaxArr[i].set(driveControllerArr[i].calculate(1, -1));   
        }
    }

    /**
     * Closes the scanner, and resets the timer.
     */
    public void endRead() {
        scanner.close();

        ReadTimer.stop();
        ReadTimer.reset();
    }
}