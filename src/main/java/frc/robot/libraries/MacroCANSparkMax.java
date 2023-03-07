package frc.robot.libraries;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;

/**
 * A POSITIONAL macro system.
 * Theoreritcally more relaible than
 * an input based macro system.
 */
public class MacroCANSparkMax {
    /**
     * {@link CANSparkMax} declared by initalization
     */
    public final CANSparkMax motor;

    /**
     * = {@link edu.wpi.first.wpilibj.Filesystem}.getDeployDirectory().getPath() + "\\Name";
     */ public final String filePath;
    
    FileWriter writer;

    Scanner scanner;

    Timer recordTimer = new Timer(), readTimer = new Timer();

    double preveousReadTime = 0;

    /**
     * Default PID values
     */
    final double kP = 0.1, kI = 0, kD = 0;

    final PIDController pidDriver;
    
    /**
     * Stores a single CANSparkMax to support macro
     * reading and writing
     * 
     * @param Motor CANSparkMax instance to modify
     * @param Name Identification name for file system, if a file by the
     * same name already exists, it will be overwritten
     * 
     * @throws IOException if FileWriter is unable to be created
     */
    public MacroCANSparkMax(CANSparkMax Motor, String Name) {
        filePath = Filesystem.getDeployDirectory().getPath() + "\\" + Name;

        motor = Motor;

        pidDriver = new PIDController(kP, kI, kD, 0);
    }

    /**
     * Sets the P I and D values of the
     * integrated {@link PIDController}
     * @param P Proportion of Error
     * @param I Integral of Error
     * @param D Derivative of Error
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax setPID(double P, double I, double D) {
        pidDriver.kP = P;
        pidDriver.kI = I;
        pidDriver.kD = D;

        return this;
    }

    /**
     * Initializes file writer
     * 
     * @throws IOException if writer fails to initalize
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax beginRecord() throws IOException {

        // Clear the file
        FileWriter fwOb = new FileWriter(filePath, false); 
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();

        // Create a temporary FileWriter
        writer = new FileWriter(filePath, true);

        recordTimer.start();

        return this;
    }

    /**
     * Records a single frame on a new line within the active file
     * 
     * @throws IOException if writer is unable to write
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax recordFrame() throws IOException {

        // if the writer does not exist, attempt to create it
        if (writer.equals(null)) {
            beginRecord(); 
        }

        // Write the time and position into the file, and end it with a new line
        writer.write(recordTimer.get() + "," + motor.getEncoder().getPosition() + "\n");

        return this;
    }

    /**
     * Closes file writer and ends record timer
     * 
     * @throws IOException if writer is unable to close
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax endRecord() throws IOException {
        writer.close(); 

        recordTimer.stop();
        recordTimer.reset();

        return this;
    }

    /**
     * Creates scanner object and starts reading timer
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax beginRead() {
        scanner = new Scanner(filePath);
        
        readTimer.start();

        return this;
    }

    /**
     * Checks if current read time has passed time of current line.
     * If so, position is inputted as the goal to the PID tuner, and
     * motor is ran.
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax readToMotor() {

        // if the writer does not exist, attempt to create it
        if (scanner.equals(null)) {
            beginRead(); 
        }

        // Do nothing until time has passed the indicated seconds
        if (readTimer.get() > preveousReadTime) {
            // Time is stored before position, keep this in mind!
            preveousReadTime = scanner.nextDouble();

            // Grab position, and set target
            pidDriver.setTarget(scanner.nextDouble());
        }

        // Drive motor, regardless of frame status
        pidDriver.setInput(motor.getEncoder().getPosition()); // Tell the PID controller whats up
        motor.set(pidDriver.calculate(1, -1)); // Set the motor
        
        return this;
    }

    /**
     * Closes file scanner, and resets read timer
     * 
     * @return self for chaining
     */
    public MacroCANSparkMax endRead() {
        scanner.close();

        readTimer.stop();
        readTimer.reset();

        return this;
    }

}