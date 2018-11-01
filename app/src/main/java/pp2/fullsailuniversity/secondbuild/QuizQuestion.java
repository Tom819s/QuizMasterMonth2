package pp2.fullsailuniversity.secondbuild;

import java.io.Serializable;

public class QuizQuestion implements Serializable {
    String questionString;
    Answer[] answers;

    QuizQuestion(String qstring, Answer ans1, Answer ans2, Answer ans3, Answer ans4) {
        questionString = qstring;
        answers = new Answer[4];
        answers[0] = ans1;
        answers[1] = ans2;
        answers[2] = ans3;
        answers[3] = ans4;
    }

    @Override
    public String toString() {
        String quizString = new String();
        quizString = "QUIZ QUESTION\n";
        quizString += questionString + '\n' + answers[0] + '\n' + answers[1] + '\n' + answers[2] + '\n' + answers[3] + '\n' + whichIsRight();
        return quizString;
    }

    public void RandomizeQuestionOrder() {
        //using a simplified fischer-yates algorithm to swap answers randomly
        //this is used because otherwise the first answer would always be correct due to how the API sends data to the app
        for (int i = answers.length - 1; i > 0; i--) {
            int j = (int) Math.floor(Math.random() * (i + 1));
            Answer temp = answers[i];
            answers[i] = answers[j];
            answers[j] = temp;
        }
    }

    private int whichIsRight(){
        int isRight = 5;
        for (int i = 0; i < 4; ++i)
        {
            if (answers[i].isCorrect)
            isRight = i;
        }
        return isRight;
    }

}