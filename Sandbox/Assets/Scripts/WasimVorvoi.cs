using NUnit.Framework;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;
using UnityEngine.Subsystems;

public class Practice : MonoBehaviour
{
    private float num; //attribute
    
    public float Num //property
    {
        get { return num; }
        set { num = value; }
    }
    private Vector3 values; //attribute

    public Vector3 Values //property
    {
        get { return values; }
        set { values = value; }
    }
    void Start()
    {
        Debug.Log("Starting");
        MyList();
        ListMath();
        Stringmeth();
        Hey();
    }
    void Update()
    {
        DoSomething();
    }
    void Hey() {
        Values = new Vector3(
            Random.Range(-100, 100), 
            Random.Range(-5, 5), 
            Random.Range(20, 120));
        Debug.Log(Values);
        Values = new Vector3(
            Mathf.Clamp(Values.x, -10, 10),
            Mathf.Clamp(Values.y, -5, 5),
            Mathf.Clamp(Values.z, 30, 90));
        Debug.Log(Values);
    }

    void Stringmeth() { 
        List<string> myList = new List<string>() {"hello", "longest", "word", "goodbye!"};
        string longest = "";
        string secongdLongest = "";
        foreach (string s in myList) {
            if (s.Length > longest.Length) {
                secongdLongest = longest;
                longest = s;
            }
            else if (s.Length > secongdLongest.Length) {
                secongdLongest = s;
            }
        }
        Debug.Log("Longest word: " + longest + " " + longest.Length + ", Second longest word: " + secongdLongest + " " + secongdLongest.Length);
    }
    void ListMath()
    {
        List<int> myList = new List<int>();
        for (int i = 0; i < 5000; i++)
        {
            if (myList.Count != 500) {
                if ((i % 2 == 0) && (i % 3 == 0) && (i % 10 != 0)) {
                    myList.Add(i);
                }
            }
            else {
                break;
            }
        }
        int avg = 0;
        foreach (int i in myList)
        {
            avg += i;
        }
        Debug.Log("Average: " + avg);
        int sum = avg / 500;
        Debug.Log("Sum: " + sum);

    }


    void MyList()
    {
        List<int> myList = new List<int>();
        for (int i = 0; i < 5000; i++)
        {
            if (myList.Count != 500) {
                if ((i % 2 == 0) && (i % 3 == 0) && (i % 10 != 0)) {
                    myList.Add(i);
                }
            }
            else {
                break;
            }
        }
        foreach (int i in myList)
        {
            Debug.Log(i);
        }
    }
    void DoSomething() {
        Vector3 Values = new Vector3(
            Mathf.PingPong(Num, 10),
            Mathf.PingPong(Num, 10),
            Mathf.PingPong(Num, 10));
        transform.localPosition = Values;
        Num+=0.01f;
    }

}
