package frc.robot.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.libraries.AutoOperationMode.OperationMode;
import frc.robot.libraries.AutoOperationMode.OperationTag;

/**
 * This class acts as an extention to CommandBase, allowing the command
 * to function within the AutoCommandScheduler
 */
public abstract class AutoCommandBase extends CommandBase {

    /**
     * Allows child to communicate with parent
     */
    protected AutoCommandScheduler parentScheduler;
    // A better immagration policy than Trump's

    /**
     * Runs the command repeatedly when its condition is true
     */ protected static OperationMode whileTrue = new AutoOperationMode.c_WhileTrue();
    /**
     * Runs the command repeatedly when its condition is false
     */ protected static OperationMode whileFalse = new AutoOperationMode.c_WhileFalse();
    /**
     * Runs the command once when its condition is true
     */ protected static OperationMode onTrue = new AutoOperationMode.c_OnTrue();
    /**
     * Runs the command once when its condition is false
     */ protected static OperationMode onFalse = new AutoOperationMode.c_OnFalse();

    /**
     * Disables default command operation while command is running
     */ protected OperationTag pauseDefaultOperation = new AutoOperationMode.c_PauseDefaultCommand(this);
    
    // Cannot be final, despite the defining function typically running in the constructor
    public BooleanSupplier condition = null;   // Null to throw exception when improperly constructed
    public OperationMode operationCondition = null;
    public List<OperationTag> operationTagArr = new ArrayList<OperationTag>();

    /**
     * Declares the operating condition of the command
     * for the scheduler polling operations
     * 
     * @param PollCondition A booleanSupplier referring to a function
     * that returns true when the command should be scheduled
     * 
     * @return self for chaining
     */
    protected AutoCommandBase declareCondition(BooleanSupplier PollCondition) {
        condition = PollCondition;

        return this;
    }

    /**
     * Declares the operation mode of the command for scheduler
     * polling operations
     * 
     * @param OperationModeCondition OperationMode defining how the function
     * should be ran when its poll condition returns true
     * 
     * @return self for chaining
     * 
     * @see AutoOperationMode.OperationMode
     */
    protected AutoCommandBase declareOperationMode(OperationMode OperationModeCondition) {
        operationCondition = OperationModeCondition;

        return this;
    }
}
