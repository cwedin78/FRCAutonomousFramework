package frc.robot.libraries;

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

    static class c_WhileTrue extends OperationMode {
        @Override
        protected boolean TruePoll() {return true;}
        @Override
        protected boolean FalsePoll() {return false;}
    }

    static class c_WhileFalse extends OperationMode {
        @Override
        protected boolean TruePoll() {return false;}
        @Override
        protected boolean FalsePoll() {return true;}
    }

    static class c_OnTrue extends OperationMode {
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

    static class c_OnFalse extends OperationMode {
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

    static class c_PauseDefaultCommand extends OperationTag {
        boolean tlock = false, flock = false;

        private final AutoCommandBase thisCommandBase;

        private AutoCommandBase[] pausedDefaultCommandArr;

        public c_PauseDefaultCommand(AutoCommandBase self) {
            thisCommandBase = self;
        }

        @Override
        protected void TruePoll() {

            flock = false;

            if (!tlock) { // Inline run once operation

                // Grab default command set
                pausedDefaultCommandArr = thisCommandBase.parentScheduler.getDefaultCommand();

                // Empty the default command set in the scheduler
                thisCommandBase.parentScheduler.setDefaultCommand();

                // Pause default command timer
                thisCommandBase.parentScheduler.DefaultCommandTimer.stop();

                tlock = true;
            }
        }

        @Override
        protected void FalsePoll() {

            tlock = false;

            if (!flock) {

                // Add back default commands
                thisCommandBase.parentScheduler.setDefaultCommand(pausedDefaultCommandArr);

                // Unpause default command timer
                thisCommandBase.parentScheduler.DefaultCommandTimer.start();

                flock = true;
            }
        }
        
    }
}
