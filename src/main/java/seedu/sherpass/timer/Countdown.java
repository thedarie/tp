package seedu.sherpass.timer;

import seedu.sherpass.task.TaskList;
import seedu.sherpass.util.Ui;
import seedu.sherpass.util.timetable.Timetable;

import javax.swing.JFrame;
import javax.swing.JLabel;


import static seedu.sherpass.constant.Message.EMPTY_STRING;
import static seedu.sherpass.constant.TimerConstant.NO_TIME_LEFT;
import static seedu.sherpass.constant.TimerConstant.ONE_MINUTE;
import static seedu.sherpass.constant.TimerConstant.ONE_HOUR;

public class Countdown extends Timer  {

    private boolean hasTimeLeft = false;
    protected int timeLeft;

    private final JFrame jframe;
    private final JLabel jlabel;

    /**
     * Creates a constructor for timer. Initialises the parameters needed for the countdown timer.
     *
     * @param ui UI
     */
    public Countdown(TaskList taskList, Ui ui, JFrame jframe, JLabel jlabel) {
        super(taskList, ui);
        timeLeft = NO_TIME_LEFT;
        this.jframe = jframe;
        this.jlabel = jlabel;
    }

    private String convertTimeToString() {
        long hour;
        long minute;
        long second;
        if ((timeLeft / ONE_HOUR) > 0) {
            hour = timeLeft / ONE_HOUR;
            minute = (long) ((timeLeft * 1.0) / ONE_HOUR) * 60;
            second = timeLeft - (hour * ONE_HOUR) - (minute * ONE_MINUTE);
            String zeroStringHour = (hour > 9) ? EMPTY_STRING : "0";
            String zeroStringMinute = (minute > 9) ? EMPTY_STRING : "0";
            String zeroStringSecond = (second > 9) ? EMPTY_STRING : "0";
            return zeroStringHour + hour + " hour(s) " + zeroStringMinute + minute
                    + " minute(s) " + zeroStringSecond + second + " second(s)";
        } else if ((timeLeft / ONE_MINUTE) > 0) {
            minute = timeLeft / ONE_MINUTE;
            second = timeLeft - (minute * ONE_MINUTE);
            String zeroStringMinute = (minute > 9) ? EMPTY_STRING : "0";
            String zeroStringSecond = (second > 9) ? EMPTY_STRING : "0";
            return zeroStringMinute + minute + " minute(s) " + zeroStringSecond + second + " second(s)";
        } else {
            second = timeLeft;
            String zeroStringSecond = (second > 9) ? EMPTY_STRING : "0";
            return zeroStringSecond + second + " second(s)";
        }
    }

    /**
     * Creates a new thread to run the timer. The timer will continue to run until it has run out of time, or has been
     * stopped by the user.
     */
    @Override
    public void run() {
        isTimerRunning = true;
        printTimerStart();
        jframe.setVisible(true);
        while (hasTimeLeft) {
            assert timeLeft > NO_TIME_LEFT;
            String timeShownToUser = convertTimeToString();
            jlabel.setText("Time left: " + timeShownToUser);
            update();
        }
        if (timerRanOutOfTime()) {
            jframe.setVisible(false);
            assert timeLeft <= NO_TIME_LEFT;
            isTimerRunning = false;
            TimerLogic.resetIsTimerInitialised();
            ui.showToUser("Time is up!\n"
                    + "Would you like to start another timer, mark a task as done, or leave the study session?");
            Timetable.showTodaySchedule(taskList, ui);
            ui.showLine();
        }
        this.interrupt();
    }

    /**
     * Updates the timer by letting the thread sleep for 1 second, then updating timeLeft. The timer will not update
     * if it is paused and will instead wait for the user to resume the timer.
     */
    protected void update() {
        try {
            Thread.sleep(1000);
            timeLeft -= 1;
            updateHasTimeLeft();
            if (isTimerPaused) {
                waitForTimerToResume();
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * Stops the timer if it is running, else prints an error message.
     */
    public void stopTimer() {
        if (isTimerRunning) {
            jframe.setVisible(false);
            ui.showToUser("Alright, I've stopped the timer.");
            isTimerRunning = false;
            forcedStop = true;
            timeLeft = NO_TIME_LEFT;
            hasTimeLeft = false;
            this.interrupt();
        } else {
            ui.showToUser("The timer has already stopped.");
        }
    }

    private void updateHasTimeLeft() {
        if (timeLeft <= NO_TIME_LEFT) {
            hasTimeLeft = false;
        }
    }

    /**
     * Returns whether timer ran out of time.
     *
     * @return Boolean of whether timer ran out of time.
     */
    private boolean timerRanOutOfTime() {
        return (!hasTimeLeft && !forcedStop);
    }

    /**
     * Prints the timer selected by the user.
     */
    protected void printTimerStart() {
        int hours;
        int minutes;
        int seconds;
        if (timeLeft >= ONE_HOUR) {
            hours = timeLeft / ONE_HOUR;
            minutes = (timeLeft - hours * ONE_HOUR) / ONE_MINUTE;
            seconds = timeLeft - hours * ONE_HOUR - minutes * ONE_MINUTE;
            ui.showToUser("Timer of " + hours + " hours " + minutes + " minutes "
                    + seconds + " seconds started.");
        } else if (timeLeft >= ONE_MINUTE) {
            minutes = timeLeft / ONE_MINUTE;
            seconds = timeLeft - (minutes * ONE_MINUTE);
            ui.showToUser("Timer of " + minutes + " minutes "
                    + seconds + " seconds started.");
        } else {
            ui.showToUser("Timer of " + timeLeft + " seconds started.");
        }
    }

    /**
     * Sets the duration of the timer, as specified by the user.
     *
     * @param duration Duration of timer in seconds.
     */
    public void setDuration(int duration) {
        timeLeft = duration;
        hasTimeLeft = true;
    }

    /**
     * Resumes the timer by calling notify() on the waiting thread.
     */
    public void resumeTimer() {
        synchronized (this) {
            isTimerPaused = false;
            notify();
        }
        ui.showToUser("Okay! I've resumed the timer. You have " + timeLeft + " seconds left.");
    }
}