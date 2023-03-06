package frc.robot.libraries;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.libraries.AutoOperationMode.OperationTag;

/**
 * This class can function as a command, thus it can be returned by
 * RobotContainer.getAutonomousCommand
 */
public class AutoCommandScheduler extends CommandBase {

    /**
     * Main autonomous process timer
     */
    public Timer AutoTimer = new Timer();

    /**
     * Default command operation timer
     */
    public Timer DefaultCommandTimer = new Timer();
    
    /**
     * Set of commands to periodically scheduler
     */
    private AutoCommandBase[] defaultCommandArr;

    /**
     * Set of commands within the auto
     */
    public AutoCommandBase[] pollCommandArr;

    /**
     * Creates a new AutoCommandScheduler, to handle a single
     * autonomous command series.
     * 
     * @param AutoCommands
     */
    public AutoCommandScheduler(AutoCommandBase... AutoCommands) {
        pollCommandArr = AutoCommands;

        // Add current instance of self to child commands
        for (AutoCommandBase autoCommandBase : AutoCommands) {
            autoCommandBase.parentScheduler = this;
        }
        // Parents need to talk to their children!!
    }

    /**
     * Defines a set of commands to run every frame during the auto.
     * Operation of default command may be subject to operation tags
     * 
     * @see AutoOperationMode.OperationTag
     * 
     * @param DefaultCommandArr
     * 
     * @return self for chaining
     */
    public AutoCommandScheduler setDefaultCommand(AutoCommandBase... DefaultCommandArr) {
        defaultCommandArr = DefaultCommandArr;

        return this;
    }

    /**
     * @return defaultCommandArr
     */
    public AutoCommandBase[] getDefaultCommand() {
        return defaultCommandArr;
    }

    @Override
    public void execute() {

        // Loop for 15 seconds
        while (AutoTimer.get() < 15) {

            for (AutoCommandBase autoCommandBase : pollCommandArr) {

                // Im building a nest to hatch my babies
                // Im so sorry for whoever reads this code

                if (autoCommandBase.condition.getAsBoolean()) {
                    if (autoCommandBase.operationCondition.TruePoll()) {
                        // Schedule the command
                        autoCommandBase.schedule();
                    }

                    // Run tag functions
                    for (OperationTag operationTag : autoCommandBase.operationTagArr) {
                        operationTag.TruePoll();
                    }
                } else {
                    if (autoCommandBase.operationCondition.FalsePoll()) {
                        // Schedule the command
                        autoCommandBase.schedule();
                    }

                    // Run tag functions
                    for (OperationTag operationTag : autoCommandBase.operationTagArr) {
                        operationTag.FalsePoll();
                    }
                }
            }

            // Schedule default commands
            for (AutoCommandBase defaultAutoCommandBase : defaultCommandArr) {
                defaultAutoCommandBase.schedule();
            }
        }
    }
}