package frc.robot.libraries.AutoFramework;

import java.util.ArrayList;
import java.util.List;

public class AutoOperationMode {
    
    /**
     * Defines how the command should be ran when its
     * condition returns true
     */
    public static abstract class OperationMode {
        /**
         * Repeatedly called while condition returns true
         * 
         * @return true if command should be ran
         */
        protected abstract boolean TruePoll();
        /**
         * Repeatedly called while condition returns false
         * 
         * @return true if command should be ran
         */
        protected abstract boolean FalsePoll();
    }

    // Quite self explanatory

    /**
     * Runs the command repeatedly when its condition is true
     */
    public static class WhileTrue extends OperationMode {
        @Override
        protected boolean TruePoll() {return true;}
        @Override
        protected boolean FalsePoll() {return false;}
    }

    /**
     * Runs the command repeatedly when its condition is false
     */
    public static class WhileFalse extends OperationMode {
        @Override
        protected boolean TruePoll() {return false;}
        @Override
        protected boolean FalsePoll() {return true;}
    }

    /**
     * Runs the command once when its condition is true
     */
    public static class OnTrue extends OperationMode {
        boolean lock = false;

        @Override
        protected boolean TruePoll() {
            boolean prev = lock;
            lock = true;
            return !prev;
        }
        @Override
        protected boolean FalsePoll() {
            return lock = false;
        }
    }

    /**
     * Runs the command once when its condition is false
     */
    public static class OnFalse extends OperationMode {
        boolean lock = false;

        @Override
        protected boolean TruePoll() {
            return lock = false;
        }
        @Override
        protected boolean FalsePoll() {
            boolean prev = lock;
            lock = true;
            return !prev;
        }
    }

    /**
     * Defines special modefiers to apply to the
     * AutoCommandScheduler when command is running
     */
    public static abstract class OperationTag {
        /**
         * Repeatedly called while condition returns true
         */
        protected abstract void TruePoll();
        /**
         * Repeatedly called while condition returns false
         */
        protected abstract void FalsePoll();
    }

    /**
     * Disables default command operation while command is running
     */
    public static class PauseDefaultCommand extends OperationTag {
        boolean tlock = false, flock = false;

        private final AutoCommandScheduler thisCommandScheduler;

        // Not null to avoid exception!
        private AutoCommandBase pausedDefaultCommand = new InstantAutoCommand();

        /**
         * Pauses the scheduler while this command is running
         * 
         * @param scheduler Provided scheduler this command will effect
         */
        public PauseDefaultCommand(AutoCommandScheduler Scheduler) {
            thisCommandScheduler = Scheduler;

            // Move current default command into the paused slot for now.
            pausedDefaultCommand = Scheduler.getDefaultCommand();
        }

        @Override
        protected void TruePoll() {

            flock = false;

            if (!tlock) { // Inline run once operation

                // Grab default command set
                pausedDefaultCommand = thisCommandScheduler.getDefaultCommand();

                // End the default command
                thisCommandScheduler.getDefaultCommand().end(false);

                // Empty the default command set in the scheduler
                thisCommandScheduler.setDefaultCommand(new InstantAutoCommand());

                // Pause default command timer
                thisCommandScheduler.DefaultCommandTimer.stop();

                tlock = true;
            }
        }

        @Override
        protected void FalsePoll() {

            tlock = false;

            if (!flock) {

                // Add back default commands
                thisCommandScheduler.setDefaultCommand(pausedDefaultCommand);

                // Start the default command
                pausedDefaultCommand.initialize();

                // Unpause default command timer
                thisCommandScheduler.DefaultCommandTimer.start();

                flock = true;
            }
        }
        
    }
}
