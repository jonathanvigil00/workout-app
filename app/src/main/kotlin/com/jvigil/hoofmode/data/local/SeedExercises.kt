package com.jvigil.hoofmode.data.local

import com.jvigil.hoofmode.data.local.entity.ExerciseEntity

/** Built-in exercise library the app ships with (requirement 8.1). */
object SeedExercises {

    fun all(): List<ExerciseEntity> = listOf(
        // Chest
        seed("Bench Press", "Chest", "Barbell"),
        seed("Incline Bench Press", "Chest", "Barbell"),
        seed("Dumbbell Bench Press", "Chest", "Dumbbell"),
        seed("Incline Dumbbell Press", "Chest", "Dumbbell"),
        seed("Dumbbell Fly", "Chest", "Dumbbell"),
        seed("Cable Crossover", "Chest", "Cable"),
        seed("Push-Up", "Chest", "Bodyweight"),
        seed("Chest Dip", "Chest", "Bodyweight"),
        // Back
        seed("Deadlift", "Back", "Barbell"),
        seed("Barbell Row", "Back", "Barbell"),
        seed("Pull-Up", "Back", "Bodyweight"),
        seed("Chin-Up", "Back", "Bodyweight"),
        seed("Lat Pulldown", "Back", "Cable"),
        seed("Seated Cable Row", "Back", "Cable"),
        seed("One-Arm Dumbbell Row", "Back", "Dumbbell"),
        seed("T-Bar Row", "Back", "Barbell"),
        seed("Face Pull", "Back", "Cable"),
        // Shoulders
        seed("Overhead Press", "Shoulders", "Barbell"),
        seed("Seated Dumbbell Press", "Shoulders", "Dumbbell"),
        seed("Arnold Press", "Shoulders", "Dumbbell"),
        seed("Lateral Raise", "Shoulders", "Dumbbell"),
        seed("Front Raise", "Shoulders", "Dumbbell"),
        seed("Rear Delt Fly", "Shoulders", "Dumbbell"),
        seed("Upright Row", "Shoulders", "Barbell"),
        seed("Shrug", "Shoulders", "Barbell"),
        // Legs
        seed("Squat", "Legs", "Barbell"),
        seed("Front Squat", "Legs", "Barbell"),
        seed("Leg Press", "Legs", "Machine"),
        seed("Romanian Deadlift", "Legs", "Barbell"),
        seed("Leg Curl", "Legs", "Machine"),
        seed("Leg Extension", "Legs", "Machine"),
        seed("Walking Lunge", "Legs", "Dumbbell"),
        seed("Bulgarian Split Squat", "Legs", "Dumbbell"),
        seed("Hip Thrust", "Legs", "Barbell"),
        seed("Calf Raise", "Legs", "Machine"),
        seed("Goblet Squat", "Legs", "Dumbbell"),
        // Arms
        seed("Barbell Curl", "Arms", "Barbell"),
        seed("Dumbbell Curl", "Arms", "Dumbbell"),
        seed("Hammer Curl", "Arms", "Dumbbell"),
        seed("Preacher Curl", "Arms", "Barbell"),
        seed("Tricep Pushdown", "Arms", "Cable"),
        seed("Skull Crusher", "Arms", "Barbell"),
        seed("Overhead Tricep Extension", "Arms", "Dumbbell"),
        seed("Close-Grip Bench Press", "Arms", "Barbell"),
        seed("Dip", "Arms", "Bodyweight"),
        // Core
        seed("Plank", "Core", "Bodyweight"),
        seed("Hanging Leg Raise", "Core", "Bodyweight"),
        seed("Cable Crunch", "Core", "Cable"),
        seed("Russian Twist", "Core", "Bodyweight"),
        seed("Ab Wheel Rollout", "Core", "Other"),
        // Full body / conditioning
        seed("Kettlebell Swing", "Full Body", "Kettlebell"),
        seed("Clean and Jerk", "Full Body", "Barbell"),
        seed("Farmer's Carry", "Full Body", "Dumbbell"),
        seed("Rowing Machine", "Full Body", "Machine"),
    )

    private fun seed(name: String, muscleGroup: String, equipment: String) =
        ExerciseEntity(name = name, muscleGroup = muscleGroup, equipment = equipment, isCustom = false)
}
