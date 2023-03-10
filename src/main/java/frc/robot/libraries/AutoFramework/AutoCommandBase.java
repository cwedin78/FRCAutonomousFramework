package frc.robot.libraries.AutoFramework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.libraries.AutoFramework.AutoOperationMode.OperationMode;
import frc.robot.libraries.AutoFramework.AutoOperationMode.OperationTag;

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

    // Cannot be final, despite the defining function typically running in the constructor
    public BooleanSupplier[] condition;   // Null to throw exception when improperly constructed
    public OperationMode operationCondition = new AutoOperationMode.WhileTrue(); // Default
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
    public AutoCommandBase declareCondition(BooleanSupplier... PollCondition) {
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
    public AutoCommandBase declareOperationMode(OperationMode OperationModeCondition) {
        operationCondition = OperationModeCondition;

        return this;
    }

    /**
     * Declares special modefiers for the AutoCommandScheduler this command
     * is running within. 
     * 
     * @param OperationTagArr Any number of OperationTag variables
     * 
     * @return self for chaining
     */
    public AutoCommandBase declareOperationTag(OperationTag... OperationTagArr) {

        operationTagArr.clear();

        for (OperationTag operationTag : OperationTagArr) {
            operationTagArr.add(operationTag);
        }

        return this;
    }


    
    /**
     * For OnFalse functionality of the end() function
     */
    boolean endLock = true;
}
