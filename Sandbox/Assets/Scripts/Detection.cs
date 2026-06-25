using UnityEngine;
using TMPro;
using UnityEngine.UI;

public class Detection : MonoBehaviour
{
    private GameObject Manager;
    void Start()
    {
        Manager = GameObject.FindGameObjectWithTag("mamager");
    }
    void Update()
    {

    }
    void OnCollisionEnter(Collision collision)
    {
        Manager.GetComponent<GameManager4r>().IncrimentScore();
    }

}