using System;
using UnityEngine;
using Random = UnityEngine.Random;
using System.Threading.Tasks;

public class FirstMovement : MonoBehaviour
    
{
    private int speed;
    bool on = false;
    public int Speed
    {
        get { return speed; }
        set { speed = value; }
    }
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        Speed = 100;
    }

    // Update is called once per frame
    void Update()
    {
        
        if(Input.GetKey(KeyCode.S))
        {
            speed -= 10;
            if (speed < 0)
            {
                speed = 0;
            }
            Debug.Log(speed);
        }
        if (Input.GetKey(KeyCode.A))
        {
            transform.Rotate(Vector3.down * Time.deltaTime * Speed, Space.World);
        }
        if (Input.GetKey(KeyCode.D))
        {
            transform.Rotate(Vector3.up * Time.deltaTime * Speed, Space.World);
        }
        if (on)
        {
            if (Random.Range(1, 100) == 5) {
                float x = Random.Range(1, 30);
                float z = Random.Range(1, 30);
                //x = Mathf.Clamp(x, 1, 50);
                //z = Mathf.Clamp(z, 1, 50);
                transform.position = new Vector3(x, 0f, z);
            }
        }
        if (Input.GetKeyDown(KeyCode.Q))
        {
            Debug.Log("Teleport" + on);
            transform.position = new Vector3(0f, 0f, 0f);
            on = !on;
        }

    }
}
