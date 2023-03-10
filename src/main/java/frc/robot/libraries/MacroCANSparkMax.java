package frc.robot.libraries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;

public class MacroCANSparkMax {

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
    public final CANSparkMax canSparkMax;

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
    PIDController driveController = new PIDController(0.1, 0, 0, 0);

    /**
     * Creates an instance of a class extending the function of a 
     * {@link com.revrobotics.CANSparkMax} to allow for positional macro use.
     * 
     * @param Motor CANSparkMax to modify
     * @param Id identifier for file system
     * 
     * @throws IOException when file location is innaccessable.
     */
    public MacroCANSparkMax(CANSparkMax Motor, String Id) throws IOException {
        canSparkMax = Motor;

        name = Id;

        mapFile = new File( dataPath + "\\" + name + ".txt");
    }

    /**
     * Sets the P, I, and D values of the embedded {@link PIDController}
     * @param kP Proportion of the error
     * @param kI Proportion of the integral of the error
     * @param kD Proportion of the derivative of the error
     * 
     * @return this for chaining
     */
    public MacroCANSparkMax setPID(double kP, double kI, double kD) {

        driveController.kP = kP;
        driveController.kI = kI;
        driveController.kD = kD;

        return this;
    }

    /**
     * Starts the record timer and institates the 
     * writer, and clears the file at the defined path.
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

        canSparkMax.getEncoder().setPosition(0); // Let the user do this at the right time

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
     * to the macro file
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
        String toWrite = RecordTimer.get() + "," + canSparkMax.getEncoder().getPosition() + "\n";

        writer.append(toWrite);
    }

    /**
     * Closes the file writer, and resets the timer.
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

        driveController.setInput(canSparkMax.getEncoder().getPosition());

        // Only progress to read the position if the time has passed the
        // time defined within the macro.
        if (ReadTimer.get() > lastReadTimer) {

            if (scanner.hasNext()) {
                driveController.setTarget(scanner.nextDouble());
                lastReadTimer = scanner.nextDouble();
            }
        }

        // set the motor
        canSparkMax.set(driveController.calculate(1, -1));
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