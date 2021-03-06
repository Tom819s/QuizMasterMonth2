package pp2.fullsailuniversity.secondbuild;

import android.util.Log;

import java.io.Serializable;

public class QuizQuestion implements Serializable
{
    String questionString;
    Answer[] answers;
    boolean isTrueFalse, correctAns;

    QuizQuestion(String qstring, Answer ans1, Answer ans2, Answer ans3, Answer ans4)
    {

        questionString = qstring;
        answers = new Answer[4];
        answers[0] = ans1;
        answers[1] = ans2;
        answers[2] = ans3;
        answers[3] = ans4;
        isTrueFalse = false;
    }

    QuizQuestion(String qstring, boolean isTruth)
    {

        questionString = qstring;
        correctAns = isTruth;
        isTrueFalse = true;
    }


    @Override
    public String toString()
    {
        String quizString = new String();

        Log.d("quizquestion to string", "toString: " + questionString);
        if (answers != null)
        {
            Log.d("quizquestion to string ", "toString: " + answers[0].m_answer + '\n' + whichIsRight());
            quizString = "QUIZ QUESTION\n";
            quizString += questionString + '\n' + answers[0].m_answer + '\n' + answers[1].m_answer + '\n' + answers[2].m_answer + '\n' + answers[3].m_answer + '\n' + whichIsRight();
        } else
        {
            quizString = "QUIZ QUESTION TF\n";
            quizString += questionString + '\n' + correctAns;
        }
        return quizString;
    }

    public void RandomizeQuestionOrder()
    {
        //using a simplified fischer-yates algorithm to swap answers randomly
        //this is used because otherwise the first answer would always be correct due to how the API sends data to the app
        if (answers != null)
        {
            for (int i = answers.length - 1; i > 0; i--)
            {
                int j = (int) Math.floor(Math.random() * (i + 1));
                Answer temp = answers[i];
                answers[i] = answers[j];
                answers[j] = temp;
            }
        }
    }

    private int whichIsRight()
    {
        int isRight = 5;
        for (int i = 0; i < answers.length; ++i)
        {
            if (answers[i].isCorrect)
                isRight = i;
        }
        return isRight;
    }

}