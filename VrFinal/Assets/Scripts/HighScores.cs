using UnityEngine;
using TMPro;

public class HighScores : MonoBehaviour
{
    public TMP_Text displayText;

    private string key1 = "highscore_1";
    private string key2 = "highscore_2";
    private string key3 = "highscore_3";
    public int defaultScore;

    public string title;
    public string entry1Prefix;
    public string entry2Prefix;
    public string entry3Prefix;

    void Start()
    {
        ShowScores();
    }

    public void TrySaveScore(int newScore)
    {
        int s1 = PlayerPrefs.GetInt(key1, defaultScore);
        int s2 = PlayerPrefs.GetInt(key2, defaultScore);
        int s3 = PlayerPrefs.GetInt(key3, defaultScore);

        if (newScore > s1)
        {
            s3 = s2;
            s2 = s1;
            s1 = newScore;
        }
        else if (newScore > s2)
        {
            s3 = s2;
            s2 = newScore;
        }
        else if (newScore > s3)
        {
            s3 = newScore;
        }

        PlayerPrefs.SetInt(key1, s1);
        PlayerPrefs.SetInt(key2, s2);
        PlayerPrefs.SetInt(key3, s3);
        PlayerPrefs.Save();

        ShowScores();
    }

    public void ShowScores()
    {
        int s1 = PlayerPrefs.GetInt(key1, defaultScore);
        int s2 = PlayerPrefs.GetInt(key2, defaultScore);
        int s3 = PlayerPrefs.GetInt(key3, defaultScore);

        displayText.text = title + entry1Prefix + s1 + entry2Prefix + s2 + entry3Prefix + s3;
    }

    public void ResetScores()
    {
        PlayerPrefs.SetInt(key1, defaultScore);
        PlayerPrefs.SetInt(key2, defaultScore);
        PlayerPrefs.SetInt(key3, defaultScore);
        PlayerPrefs.Save();
        ShowScores();
    }
}
