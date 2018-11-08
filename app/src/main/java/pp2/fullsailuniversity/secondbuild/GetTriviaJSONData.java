package pp2.fullsailuniversity.secondbuild;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetTriviaJSONData extends AsyncTask<String, Void, List<QuizQuestion>> implements GetRawData.OnDownloadComplete
{
    private static final String TAG = "GetTriviaJSONData";

    private static List<QuizQuestion> mQuiz = null;
    private String mBaseURL;
    private boolean runningOnSameThread = false;

    private final OnDataAvailable mCallBack;

    interface OnDataAvailable
    {
        void onDataAvailable(List<QuizQuestion> data, DownloadStatus status);
    }

    public GetTriviaJSONData(OnDataAvailable callBack, String baseURL)
    {
        Log.d(TAG, "GetTriviaJSONData called");
        mQuiz = new ArrayList<>();
        mBaseURL = baseURL;
        mCallBack = callBack;
        Log.d(TAG, "GetTriviaJSONData: exiting");
    }

    void executeOnSameThread(String searchCriteria)
    {
        Log.d(TAG, "executeOnSameThread starts");
        runningOnSameThread = true;
        GetRawData getRawData = new GetRawData(this);
        getRawData.runInSameThread(mBaseURL);
        Log.d(TAG, "executeOnSameThread ends");
    }

    @Override
    protected List<QuizQuestion> doInBackground(String... strings)
    {
        Log.d(TAG, "doInBackground starts");
        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(mBaseURL);
        Log.d(TAG, "doInBackground ends");
        return mQuiz;
    }

    @Override
    protected void onPostExecute(List<QuizQuestion> quizQuestions)
    {
        Log.d(TAG, "onPostExecute: starts");
        if (mCallBack != null)
        {
            mCallBack.onDataAvailable(quizQuestions, DownloadStatus.OK);
            MainGameActivity.quiz = quizQuestions;
        }
        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status)
    {
        Log.d(TAG, "onDownloadComplete starts. Status = " + status);

        if (status == DownloadStatus.OK)
        {

            try
            {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("results");

                for (int i = 0; i < itemsArray.length(); i++)
                {
                    JSONObject jsonQuestion = itemsArray.getJSONObject(i);
                    String question = jsonQuestion.getString("question");
                    question = fromHTML(question);
                    JSONArray incorrect_answers_array = jsonQuestion.getJSONArray("incorrect_answers");
                    Answer ans1 = new Answer(jsonQuestion.getString("correct_answer"), true);
                    ans1.m_answer = fromHTML(ans1.m_answer);
                    Answer ans2 = new Answer(incorrect_answers_array.get(0).toString(), false);
                    ans2.m_answer = fromHTML(ans2.m_answer);
                    QuizQuestion quizQuestion;
                    if (jsonQuestion.getString("type").equals("boolean"))
                    {
                        if (ans1.m_answer.equals("True"))
                            quizQuestion = new QuizQuestion(question, true);
                        else
                            quizQuestion = new QuizQuestion(question, false);
                        quizQuestion.RandomizeQuestionOrder();
                    } else
                    {
                        Answer ans3 = new Answer(incorrect_answers_array.get(1).toString(), false);
                        ans3.m_answer = fromHTML(ans3.m_answer);
                        Answer ans4 = new Answer(incorrect_answers_array.get(2).toString(), false);
                        ans4.m_answer = fromHTML(ans4.m_answer);

                        quizQuestion = new QuizQuestion(question, ans1, ans2, ans3, ans4);
                    }
                    mQuiz.add(quizQuestion);

                    Log.d(TAG, "onDownloadComplete " + quizQuestion.questionString);
                }
            } catch (JSONException jsone)
            {
                jsone.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error processing Json data " + jsone.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }

        if (runningOnSameThread && mCallBack != null)
        {
            // now inform the caller that processing is done - possibly returning null if there was an error
            mCallBack.onDataAvailable(mQuiz, status);
        }

        Log.d(TAG, "onDownloadComplete ends");
    }

    private String fromHTML(String input)
    {
        return Html.fromHtml(input).toString();
    }
}

