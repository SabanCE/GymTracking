package com.example.gymtracking

object ExerciseLibrary {

    val categories = mapOf(
        "Göğüs" to listOf(
            "Bench Press",
            "Incline Bench Press",
            "Decline Bench Press",
            "Dumbbell Bench Press",
            "Incline Dumbbell Press",
            "Decline Dumbbell Press",
            "Chest Fly",
            "Dumbbell Fly",
            "Cable Fly",
            "Machine Chest Press",
            "Push-ups",
            "Incline Push-ups",
            "Decline Push-ups",
            "Close Grip Push-ups",
            "Chest Dips",
            "Svend Press",
            "Incline Chest Press",
            "Low to High Cable Fly",
            "High to Low Cable Fly",
            "Middle Cable Fly Machine"

        ),


    "Sırt" to listOf(
    "Deadlift",
    "Romanian Deadlift",
    "Rack Pull",
    "Lat Pulldown",
    "Wide Grip Lat Pulldown",
    "Close Grip Lat Pulldown",
     "Wide Grip Cable Row",
    "Pull-ups",
    "Chin-ups",
    "Bent Over Row",
    "Barbell Row",
    "Dumbbell Row",
    "T-Bar Row",
    "Seated Cable Row",
    "Single Arm Cable Row",
    "Face Pull",
    "Straight Arm Pulldown",
    "Reverse Fly",
    "Hyperextensions",

    ),

    "Omuz" to listOf(
    "Overhead Press",
    "Barbell Shoulder Press",
    "Dumbbell Shoulder Press",
    "Arnold Press",
    "Lateral Raise",
    "Dumbbell Lateral Raise",
    "Cable Lateral Raise",
    "Front Raise",
    "Plate Front Raise",
    "Reverse Pec Deck",
    "Rear Delt Fly",
    "Upright Row",
    "Handstand Push-ups"
    ),

    "Bacak" to listOf(
    "Squat",
    "Back Squat",
    "Front Squat",
        "Hack Squat",
    "Goblet Squat",
    "Leg Press",
    "Lunge",
     "Smith Machine Squat",
     "Romanian Deadlift",
    "Walking Lunge",
    "Bulgarian Split Squat",
    "Step-ups",
    "Leg Extension",
    "Leg Curl",
    "Lying Leg Curl",
    "Seated Leg Curl",
    "Romanian Deadlift",
    "Hip Thrust",
    "Glute Bridge",
    "Calf Raise",
    "Seated Calf Raise",
    "Standing Calf Raise"
    ),

    "Kol" to listOf(
    "Barbell Curl",
    "Dumbbell Curl",
    "Bicep Curl",
    "Hammer Curl",
    "Concentration Curl",
    "Preacher Curl",
    "Cable Curl",
    "EZ Bar Curl",
    "Tricep Extension",
    "Overhead Tricep Extension",
    "Cable Tricep Pushdown",
    "Rope Pushdown",
    "Skullcrusher",
    "Close Grip Bench Press",
    "Dips",
    "Kickbacks"
    ),

    "Karın" to listOf(
    "Crunch",
    "Sit-ups",
    "Hanging Leg Raise",
    "Leg Raise",
    "Reverse Crunch",
    "Plank",
    "Side Plank",
    "Russian Twist",
    "Mountain Climbers",
    "Ab Wheel Rollout",
    "Cable Crunch",
    "Bicycle Crunch"
    )


    )


    // Tüm isimlerin düz listesi (Arama için)
    val allExercises = categories.values.flatten().sorted()
}

val quotes = listOf(
    "Bugün yapamadıkların, yarınki sınırlarını belirler.",
    "Acı geçicidir, ama başarı sonsuzdur.",
    "Vücudun her şeyi yapabilir, ikna etmen gereken zihnindir.",
    "Disiplin, ne istediğin ile en çok ne istediğin arasındaki seçimdir.",
    "Bugün vazgeçersen, yarın baştan başlamak zorunda kalırsın.",
    "Güç, konfor alanının dışında gelişir.",
    "Her tekrar, hedefe atılan küçük bir adımdır.",
    "Başarı, pes etmeyenlerin alışkanlığıdır.",
    "Ağır gelen ağırlıklar, güçlü bir karakter oluşturur.",
    "Bugünün teri, yarının gururudur.",
    "Sınır sandığın şeyler, çoğu zaman sadece bahanedir.",
    "Güçlü olmak bir günde değil, her gün karar vermekle olur.",
    "Zor gelen setler, en çok gelişimi getirir.",
    "Pes etmek kolaydır, devam etmek karakter ister.",
    "Her antrenman, daha iyi bir versiyonuna yatırımdır.",
    "Ter dökmeden hedeflere ulaşılmaz.",
    "Bugünün çalışması, yarının gücünü oluşturur.",
    "Gerçek değişim, vazgeçmediğin günlerde başlar.",
    "Ağırlıklar ağırlaştıkça iraden güçlenir.",
    "Kendini geçemeyen, kimseyi geçemez.",
    "En zor günler, en büyük gelişimi getirir.",
    "Her tekrar, iradeni biraz daha güçlendirir.",
    "Konfor alanı kas yapmaz.",
    "Bugün yorul, yarın gurur duy.",
    "Güçlü olmak bir tercih değil, bir alışkanlıktır."
)