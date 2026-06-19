const QRCode = require("qrcode");

function parseBoothPage() {
  const rawTitle = document.title;
  console.log("[Rebro QR] document.title:", rawTitle);
  const titleMatch = rawTitle.match(/^(.+?)\s*-\s*(.+?)\s*-\s*BOOTH$/);
  if (!titleMatch) return null;

  const title = titleMatch[1].trim();
  const circleName = titleMatch[2].trim();

  const nicknameContainer = document.querySelector(
    ".home-link-container__nickname"
  );
  console.log("[Rebro QR] .home-link-container__nickname:", nicknameContainer);
  const authorNames = [];
  if (nicknameContainer) {
    const anchors = nicknameContainer.querySelectorAll("a");
    anchors.forEach((a) => {
      const name = a.textContent.trim();
      if (name) authorNames.push(name);
    });
  }

  let coverImageUrl = null;
  const slickFirst = document.querySelector(
    '[data-slick-index="0"] .market-item-detail-item-image'
  );
  if (slickFirst) {
    coverImageUrl = slickFirst.getAttribute("data-origin") || slickFirst.src;
  } else {
    const firstImg = document.querySelector(
      ".primary-image-area .market-item-detail-item-image[src]"
    );
    if (firstImg) {
      coverImageUrl = firstImg.getAttribute("data-origin") || firstImg.src;
    }
  }

  const result = {
    title,
    bookType: "DOUJIN",
    publisher: "",
    authorNames,
    circleName,
    coverImageUrl,
  };
  console.log("[Rebro QR] Parsed result:", result);
  return result;
}

function showQrModal(dataUrl, result) {
  const overlay = document.createElement("div");
  overlay.className = "rebro-qr-overlay";

  const modal = document.createElement("div");
  modal.className = "rebro-qr-modal";

  const heading = document.createElement("h2");
  heading.textContent = "Rebro QR";

  const subtitle = document.createElement("p");
  subtitle.className = "rebro-qr-subtitle";
  subtitle.textContent = result.title;

  const img = document.createElement("img");
  img.src = dataUrl;
  img.alt = "Rebro QR Code";

  const closeBtn = document.createElement("button");
  closeBtn.className = "rebro-qr-close";
  closeBtn.textContent = "閉じる";
  closeBtn.addEventListener("click", () => overlay.remove());

  modal.appendChild(heading);
  modal.appendChild(subtitle);
  modal.appendChild(img);
  modal.appendChild(closeBtn);
  overlay.appendChild(modal);

  overlay.addEventListener("click", (e) => {
    if (e.target === overlay) overlay.remove();
  });

  document.body.appendChild(overlay);
}

async function generateAndShowQr() {
  const result = parseBoothPage();
  if (!result) {
    alert("ページの情報を取得できませんでした");
    return;
  }

  const jsonStr = JSON.stringify(result);
  const bytes = new TextEncoder().encode(jsonStr);
  const segments = [{ data: bytes, mode: "byte" }];

  try {
    const dataUrl = await QRCode.toDataURL(segments, {
      errorCorrectionLevel: "L",
      margin: 2,
      width: 560,
      color: { dark: "#000000", light: "#ffffff" },
    });
    showQrModal(dataUrl, result);
  } catch (err) {
    console.error("Rebro QR generation failed:", err);
    alert("QRコードの生成に失敗しました: " + err.message);
  }
}

function injectButton() {
  const variationCart = document.querySelector(".variation-cart");
  if (!variationCart) {
    console.warn("[Rebro QR] .variation-cart not found");
    return;
  }

  const btn = document.createElement("button");
  btn.className = "rebro-qr-button";
  btn.textContent = "Rebro QRを表示";
  btn.addEventListener("click", generateAndShowQr);

  variationCart.parentElement.insertBefore(btn, variationCart);
}

injectButton();
