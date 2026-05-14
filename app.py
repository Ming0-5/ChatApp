from flask import Flask, request, jsonify
from textblob import TextBlob

from transformers import pipeline

app = Flask(__name__)

# -----------------------------
# LOAD AI MODEL
# -----------------------------

classifier = pipeline(
    "text-classification",
    model="unitary/toxic-bert"
)

# -----------------------------
# KEYWORD DATABASE
# -----------------------------

sensitive_words = [
    "sex",
    "nude",
    "kiss",
    "hotel",
    "private",
    "touch",
    "photo",
    "alone",
    "baby",
    "love"
]

grooming_phrases = [
    "send pic",
    "send me pic",
    "don't tell anyone",
    "keep secret",
    "meet alone",
    "come alone"
]

# -----------------------------
# ANALYZE API
# -----------------------------

@app.route('/analyze', methods=['POST'])
def analyze():

    data = request.json

    messages = data.get("messages", [])

    combined_text = " ".join(messages).lower()

    detected_words = []
    detected_phrases = []

    # -----------------------------
    # KEYWORD DETECTION
    # -----------------------------

    for word in sensitive_words:
        if word in combined_text:
            detected_words.append(word)

    for phrase in grooming_phrases:
        if phrase in combined_text:
            detected_phrases.append(phrase)

    # -----------------------------
    # AI TRANSFORMER ANALYSIS
    # -----------------------------

    ai_result = classifier(combined_text)[0]

    ai_label = ai_result['label']
    ai_score = float(ai_result['score'])

    # -----------------------------
    # RISK SCORING
    # -----------------------------

    risk_score = 0

    # keyword score
    risk_score += len(detected_words) * 10
    risk_score += len(detected_phrases) * 20

    # transformer score
    if ai_label == "toxic":
        risk_score += int(ai_score * 50)

    # custom contextual rules
    if "come to my home" in combined_text:
        risk_score += 40

    if "trust me" in combined_text:
        risk_score += 25

    if "don't tell your parents" in combined_text:
        risk_score += 50

    if "meet me tonight" in combined_text:
        risk_score += 40

    percentage = min(risk_score, 100)

    # -----------------------------
    # RISK LEVEL
    # -----------------------------

    if percentage <= 30:
        risk = "LOW RISK"

    elif percentage <= 70:
        risk = "MEDIUM RISK"

    else:
        risk = "HIGH RISK"

    # -----------------------------
    # SENTIMENT ANALYSIS
    # -----------------------------

    blob = TextBlob(combined_text)

    sentiment_score = blob.sentiment.polarity

    if sentiment_score > 0:
        sentiment = "Positive"

    elif sentiment_score < 0:
        sentiment = "Negative"

    else:
        sentiment = "Neutral"

    # -----------------------------
    # CONTEXTUAL ANALYSIS
    # -----------------------------

    contextual_analysis = (
        f"AI detected '{ai_label}' behavior "
        f"with confidence score {round(ai_score, 2)}."
    )

    # -----------------------------
    # RETURN RESPONSE
    # -----------------------------

    return jsonify({

        "sentiment": sentiment,

        "sentiment_score": sentiment_score,

        "detected_words": detected_words,

        "detected_phrases": detected_phrases,

        "percentage": percentage,

        "risk": risk,

        "contextual_analysis": contextual_analysis
    })


# -----------------------------
# RUN SERVER
# -----------------------------

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)