using System.Text;
using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Manager : MonoBehaviour
{
    public GameObject PausePanel;
    bool isPaused;
    int score = 0;
    public TMP_Text scoreText;
    private GameObject Speed;
    public TMP_Text highScoreText;
    int highScore;
    int slot = 0;
    string slotname;
    public TMP_Text speed;
    public GameObject StartPanel;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        isPaused = false;
        score = 0;
        Speed = GameObject.FindGameObjectWithTag("Player");
        highScore = PlayerPrefs.GetInt("HighScore", 0);
        Time.timeScale = 0;
        PausePanel.SetActive(false);
    }

    // Update is called once per frame
    void Update()
    {
        Paused();
        if (Input.GetKeyDown(KeyCode.R))
        {
            PlayerPrefs.DeleteKey(slotname);
        }
        if (Input.GetKeyDown(KeyCode.I))
        {
            slot = 1;
            slotname = "HighScore" + slot;
            highScore = PlayerPrefs.GetInt(slotname, 0);
        }
        if (Input.GetKeyDown(KeyCode.O))
        {
            slot = 2;
            slotname = "HighScore" + slot;
            highScore = PlayerPrefs.GetInt(slotname, 0);
        }
        if (Input.GetKeyDown(KeyCode.P))
        {
            slot = 3;
            slotname = "HighScore" + slot;
            highScore = PlayerPrefs.GetInt(slotname, 0);
        }
        if (Input.GetKeyDown(KeyCode.Space))
        {
            StartPanel.SetActive(false);
            Time.timeScale = 1;
        }
    }
    void FixedUpdate()
    {
        scoreText.text = "Score: " + score;
        highScoreText.text = "High Score: " + highScore;
        speed.text = "Speed: " + Speed.GetComponent<Movement>().Speed;
        if (score >= 20)
        {
            SceneManager.LoadScene(3);
        }
        if (score > highScore)
        {
            highScore = score;
            PlayerPrefs.SetInt(slotname, highScore);
            PlayerPrefs.Save();
        }

    }
    void Paused()
    {
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            isPaused = !isPaused;
        }
        PausePanel.SetActive(isPaused);
        if (isPaused)
        {
            Time.timeScale = 0;
        }
        else if(!isPaused && StartPanel.active == false)
        {
            Time.timeScale = 1;
        }

    }
    public void IncrimentScore()
    {
        score++;
    }
    public void resume()
    {
        isPaused = false;
        Time.timeScale = 1;
    }
    public void quit()
    {
        Application.Quit();
    }
    public void bad()
    {
        score--;
    }
    public void special()
    {
        Speed.GetComponent<Movement>().addSpeed();
    }

}
