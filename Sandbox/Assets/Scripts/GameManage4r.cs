using TMPro;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.SocialPlatforms.Impl;

public class GameManager4r : MonoBehaviour
{
    public GameObject PausePanel;
    public GameObject hoop;
    int score = 0;
    public TMP_Text scoreText;
    bool isPaused;
    void Start()
    {
        score = 0;
        isPaused = false;
        for (int z = 50; z < 300; z+=15)
        {
            float x = Random.Range(-40, 36);
            float y = Random.Range(0,41);
            Instantiate(hoop, new Vector3(x,y,z), Quaternion.identity);
        }
        score = 0;
    }
    void Update()
    {
        Paused();
    }
    void FixedUpdate()
    {
        scoreText.text = "Score: " + score;
    }
    void Paused() { 
        if(Input.GetKeyDown(KeyCode.Escape))
        {
            isPaused = !isPaused;
        }
        PausePanel.SetActive(isPaused);

    }
    public void ExitGame()
    {
        Application.Quit();
    }
    
    public void SwapScene()
    {
        if(SceneManager.GetActiveScene().buildIndex == 0)
            SceneManager.LoadScene(1);
        else
            SceneManager.LoadScene(0);
    }
    public void IncrimentScore()
    {
        score++;
    }
}
