package frc.robot.libraries;

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
     * {@link CANSparkMax} declared by initalization
     */
    public final CANSparkMax motor;

    /**
     * = {@link edu.wpi.first.wpilibj.Filesystem}.getDeployDirectory().getPath() + "\\Name";
     */ public final String filePath;
    
    FileWriter writer;

    Scanner scanner;

    Timer recordTimer = new Timer(), readTimer = new Timer();
    
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
    }

    /**
     * Initializes file writer
     * 
     * @throws IOException if writer fails to initalize
     */
    public void beginRecord() throws IOException {

        // Clear the file
        FileWriter fwOb = new FileWriter(filePath, false); 
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();

        // Create a temporary FileWriter
        writer = new FileWriter(filePath, true);

        recordTimer.start();
    }

    /**
     * Records a single frame on a new line within the active file
     * 
     * @throws IOException if writer is unable to write
     */
    public void recordFrame() throws IOException {

        // if the writer does not exist, attempt to create it
        if (writer.equals(null)) {
            beginRecord(); 
        }

        // Write the time and position into the file, and end it with a new line
        writer.write(recordTimer.get() + "," + motor.getEncoder().getPosition() + "\n");
    }

    /**
     * Closes file writer and ends record timer
     * 
     * @throws IOException if writer is unable to close
     */
    public void endRecord() throws IOException {
        writer.close(); 

        recordTimer.stop();
        recordTimer.reset();
    }

    /**
     * Creates scanner object and starts reading timer
     */
    public void beginRead() {
        scanner = new Scanner(filePath);
        
        readTimer.start();
    }

    /**
     * Checks if current read time has passed time of current line.
     * If so, position is inputted as the goal to the PID tuner, and
     * motor is ran.
     */
    public void readToMotor() {
        // TODO
    }

    /**
     * Closes file scanner, and resets read timer
     */
    public void endRead() {
        scanner.close();

        readTimer.stop();
        readTimer.reset();
    }

}