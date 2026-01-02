"use client"
import { useEffect, useState } from "react";
import { workService } from "@/services/workService";

export default function Home() {
  const [message, setMessage] = useState("Loading...");

  useEffect(() => {
    // Define an internal async function to fetch data
    const fetchMessage = async () => {
      try {
        const data = await workService.getHomeMessage();
        setMessage(data);
      } catch (error) {
        setMessage("Error loading message.");
        console.error(error);
      }
    };

    fetchMessage();
  }, []); // Empty array means this runs once when the component mounts

  return (
    <div>
      {/* Use curly braces {message} to render the variable, not the string "message" */}
      <h1>{message}</h1>
    </div>
  );
}