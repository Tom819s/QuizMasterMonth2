package pp2.fullsailuniversity.secondbuild;

public class QuizQuestion
{
    String questionString;
    Answer[] answers;

    QuizQuestion(String qstring, Answer ans1, Answer ans2, Answer ans3, Answer ans4){
        questionString = qstring;
        answers = new Answer[4];
        answers[0] = ans1;
        answers[1] = ans2;
        answers[2] = ans3;
        answers[3] = ans4;
    }

    public void RandomizeQuestionOrder()
        {
            //using a simplified fischer-yates algorithm to swap answers randomly
            //this is used because otherwise the first answer would always be correct due to how the API sends data to the app
            for (int i = answers.length - 1; i > 0; i--)
            {
                int j = (int) Math.floor(Math.random() * (i + 1));
                Answer temp = answers[i];
                answers[i] = answers[j];
                answers[j] = temp;
            }
    }

}