package pp2.fullsailuniversity.secondbuild;

public class TriviaDBURLcreator {

    public String mCategory, mDifficulty;
    public int mNumQuestions;

    public String createURL() throws java.io.IOException
    {

        int categoryAsInt;
        StringBuilder newURL = new StringBuilder();
        newURL.append("https://opentdb.com/api.php");
        if (mNumQuestions >= 9 && mNumQuestions <= 30)
        {
            newURL.append("?amount=" + mNumQuestions);
        }
        else //by default gets 10 questions otherwise
            newURL.append("?amount=10");

        if (mCategory != null)
        {
            switch (mCategory)
            {
                case "General Knowledge":
                    categoryAsInt = 9;
                break;
                case "Film":
                    categoryAsInt = 11;
                break;
                case "Science":
                    categoryAsInt = 17;
                break;
                case "Geography":
                    categoryAsInt = 22;
                break;
                default:
                    categoryAsInt = 0;
                break;
            }
                //if not valid category string passed in, get any random category by default
             if (categoryAsInt != 0) newURL.append("&category=" +categoryAsInt);
        }


        if (mDifficulty != null)
        {
            switch (mDifficulty)
            {
                case "Easy": newURL.append("&difficulty=easy");
                    break;
                case "Medium": newURL.append("&difficulty=medium");
                    break;
                case "Hard": newURL.append("&difficulty=hard");
                    break;
                default:
                    break;
                    // if no specific difficulty selected, allow all kinds to be used
            }
        }
        //currently only support multi-choice
        //TODO add code to support true/false as well
        newURL.append("&type=multiple");

        return newURL.toString();

    }
//end of class
//
}
