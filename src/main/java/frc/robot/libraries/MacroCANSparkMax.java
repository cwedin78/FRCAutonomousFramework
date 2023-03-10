package frc.robot.libraries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class MacroCANSparkMax {

    public final String dataPath = Filesystem.getDeployDirectory().getPath();

    public final String name;

    public final File mapFile;

    private FileWriter writer;
    private Scanner scanner;

    final RelativeEncoder encoder;
    final CANSparkMax motor;

    final Timer RecordTimer = new Timer();
    final Timer ReadTimer = new Timer();

    double lastReadTimer = 0;

    PIDController driveController = new PIDController(0.1, 0, 0, 0);

    /**
     * Creates an instance of a class extending the function of a 
     * {@link com.revrobotics.CANSparkMax} to allow for positional macro use
     * 
     * @param Motor CANSparkMax to modify
     * @param Id identifier for file system
     * 
     * @throws IOException when FileWriter fails to initialize
     */
    public MacroCANSparkMax(CANSparkMax Motor, String Id) throws IOException {
        encoder = Motor.getEncoder();
        motor = Motor;
        name = Id;

        mapFile = new File( dataPath + "\\" + name + ".txt");
    }

    /**
     * Starts the record timer and institates the writer.
     * Also clears the active file to begin writing
     */
    public void beginRecord() throws IOException {

        // encoder.setPosition(0); // fuck you batman

        RecordTimer.restart();
        RecordTimer.start();

        writer = new FileWriter(mapFile, true);

        // Clear the file
        PrintWriter writer = new PrintWriter(mapFile);
        writer.print(""); // Write empty
        writer.close();
    }

    /**
     * Record a single frame, write the time, and motor position,
     * to the macro file
     * 
     * @param TimeSeconds Current macro time
     */
    public void recordFrame() {

        String toWrite = RecordTimer.get() + "," + encoder.getPosition() + "\n";

        try {
            writer.append(toWrite);
        } catch (IOException e) {
            DriverStation.reportWarning("Failed to write macro line", e.getStackTrace());
        }
    }

    /**
     * Closes the file writer
     */
    public void endRecord() {
        try {
            writer.close();
        } catch (IOException e) {
            DriverStation.reportWarning("Failed to close writer", e.getStackTrace());
        }

        // Reset the timer
        RecordTimer.stop();
        RecordTimer.reset();

        // encoder.setPosition(0); // fuck you batman
    }

    /**
     * 
     * @throws FileNotFoundException
     */
    public void beginRead() throws FileNotFoundException {
        ReadTimer.restart();
        ReadTimer.start();

        scanner = new Scanner(mapFile);
        scanner.useDelimiter(",|\\n");

        if (scanner.hasNext()) {
            SmartDashboard.putString("scanner", "" + scanner.next());
            //lastReadTimer = scanner.nextDouble();
        }
    }
    
    /**
     * 
     */
    public void readFrame() {
        driveController.setInput(encoder.getPosition());

        if (ReadTimer.get() > lastReadTimer) {
            if (scanner.hasNext()) {
                driveController.setTarget(scanner.nextDouble());
                lastReadTimer = scanner.nextDouble();
            }
        }

        motor.set(driveController.calculate(1, -1));
    }

    /**
     * 
     */
    public void endRead() {
        scanner.close();

        ReadTimer.stop();
        ReadTimer.reset();
    }
}