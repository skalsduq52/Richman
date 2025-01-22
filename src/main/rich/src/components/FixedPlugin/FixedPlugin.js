import React, { Component, useState, useEffect } from "react";

import { Dropdown, Badge, Button, Form, Container, Card, Row, Col } from "react-bootstrap";
import NotificationAlert from "react-notification-alert";


function FixedPlugin({selectedItems, setSelectedItems}) {
  const notificationAlertRef = React.useRef(null);
  const notify = (place, message) => {
    var type = "danger";

    var options = {};
    options = {
      place: place,
      message: (
          <div>
            <div>
              {message}
            </div>
          </div>
      ),
      type: type,
      icon: "nc-icon nc-bell-55",
      autoDismiss: 7,
    };
    notificationAlertRef.current.notificationAlert(options);
  };


  const [dataList, setDataList] = useState([]);
  const [stockData, setStockData] = useState({
    assetType: "암호화폐",
    assetCode: "",
    assetName: "",
    upperLimit: "",
    lowerLimit: "",
  });

  const selectedCount = Object.values(selectedItems).filter(Boolean).length;

  useEffect(() => {
    fetch("http://127.0.0.1:8080/asset") // 종목 리스트 불러오는 API 호출
        .then((response) => response.json())
        .then((data) => {
          setDataList(data)
          const initialSelectedItems = {};
          data.forEach((item) => {
            if (item.checked) {
              initialSelectedItems[item.assetCode] = item;
            }
          });
          setSelectedItems(initialSelectedItems);
        })
        .catch((error) => notify("tc", "현재 서버가 불안정합니다!"));
  }, []);

  const handleCheckboxChange = async (asset) => {

    const apiUrl = `http://127.0.0.1:8080/asset/${asset.assetCode}`;
    const isChecked = selectedItems[asset.assetCode] ? true : false;

    try {
      const response = await fetch(apiUrl, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ checked: !isChecked }),
      });

      if (!response.ok) {
        notify("tc", "현재 서버가 불안정합니다!");
      }

      setSelectedItems((prev) => {
        const newSelectedItems = { ...prev };

        if (!isChecked) {
          newSelectedItems[asset.assetCode] = asset;
        } else {
          delete newSelectedItems[asset.assetCode];
        }

        if (Object.keys(newSelectedItems).length > 4) {
          return prev;
        }

        return newSelectedItems;
      });

      setDataList((prevList) =>
          prevList.map((item) =>
              item.assetCode === asset.assetCode ? { ...item, checked: !isChecked } : item
          )
      );
    } catch (error) {
      notify("tc", "현재 서버가 불안정합니다!");
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setStockData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleDelete = async (assetCode) => {
    const apiUrl = `http://127.0.0.1:8080/asset/${assetCode}`;
    try {
      const response = await fetch(apiUrl, {
        method: "DELETE",
      });

      if (!response.ok) {
        notify("tc", "삭제 실패! 현재 서버가 불안정합니다.");
        return;
      }

      setDataList((prevList) => prevList.filter((stock) => stock.assetCode !== assetCode));
      setSelectedItems((prevSelected) => {
        const newSelected = { ...prevSelected };
        delete newSelected[assetCode]; // 해당 종목을 체크 목록에서 제거
        return newSelected;
      });
    } catch (error) {
      notify("tc", "삭제 실패! 현재 서버가 불안정합니다.");
    }

  };

  const isFormValid = stockData.assetCode.trim() !== "" &&
      stockData.upperLimit.trim() !== "" &&
      stockData.lowerLimit.trim() !== "";
  const isSaveDisabled = dataList.length >= 8 || !isFormValid;;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isSaveDisabled) return;

    const apiUrl = "http://127.0.0.1:8080/asset"

    try {
      const response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(stockData),
      });

      const result = await response.json();

      if (!response.ok) {
        if(result.duplicate) {
          notify("tc","이미 등록된 종목코드 입니다.");
        } else if(result.error) {
          notify("tc", "존재하지 않는 종목코드 입니다.");
        }else{
          notify("tc", "현재 서버가 불안정합니다!");
        }
        return;
      }

      setDataList((prevList) => [
        ...prevList,
        { assetType: result.assetType, assetCode: result.assetCode, assetName: result.assetName, upperLimit: result.upperLimit, lowerLimit: result.lowerLimit },
      ]);
      setStockData({
        assetType: "",
        assetCode: "",
        assetName:"",
        upperLimit: "",
        lowerLimit: "",
      });

    } catch (error) {
      notify("tc", "현재 서버가 불안정합니다!");
    }

    // 여기에서 API 호출 또는 상태 업데이트 로직 추가 가능
  };


  return (

      <div className="fixed-plugin">
        <div className="rna-container">
          <NotificationAlert ref={notificationAlertRef}/>
        </div>
        <Dropdown>
          <Dropdown.Toggle
              id="dropdown-fixed-plugin"
              variant=""
              className="text-white border-0 opacity-100"
          >
            <i className="fas fa-cogs fa-2x mt-1"></i>
          </Dropdown.Toggle>
          <Dropdown.Menu>
            <li className="adjustments-line d-flex align-items-center justify-content-between mb-2">
              <p>데이터 관리</p>
            </li>
            <Container fluid>
              <Row>
                관리 종목을 선택하세요(최대 4개)
                <Col md="12">
                  <Card>
                    <Card.Body>
                      {dataList.map((item, index) => (
                          <Row key={index} className="align-items-center mb-2">

                            <Col className="d-flex align-items-center">
                              <Form.Check
                                  type="checkbox"
                                  checked={!!selectedItems[item.assetCode]}
                                  onChange={() => handleCheckboxChange(item)}
                                  disabled={!selectedItems[item.assetCode] && Object.keys(selectedItems).length >= 4}
                                  className="m-1 p-2 d-flex align-items-center"
                                  style={{marginRight: "8px"}} // 체크박스 정렬
                              />
                              {item.assetName}</Col>

                            <Col xs="auto" className="d-flex justify-content-end">
                              <Button
                                  variant="danger"
                                  size="sm"
                                  onClick={() => handleDelete(item.assetCode)}
                                  style={{width: "24px", height: "24px", padding: "0", fontSize: "14px"}}
                              >
                                X
                              </Button>
                            </Col>
                          </Row>
                      ))}
                    </Card.Body>
                  </Card>
                </Col>
                추가할 종목을 입력하세요(최대 8개)
                <Col md="12">
                  <Card>
                    <Card.Body>
                      <Form onSubmit={handleSubmit}>

                        <Form.Group controlId="assetCode" className="mb-3">
                          <Form.Label>종목코드</Form.Label>
                          <Form.Control
                              type="text"
                              placeholder="예: AAPL, BTC, 005930"
                              name="assetCode"
                              value={stockData.assetCode}
                              onChange={handleChange}
                          />
                        </Form.Group>

                        <Form.Group controlId="upperLimit" className="mb-3">
                          <Form.Label>임계가격 (상한가)</Form.Label>
                          <Form.Control
                              type="number"
                              placeholder="예: 100000"
                              name="upperLimit"
                              value={stockData.upperLimit}
                              onChange={handleChange}
                          />
                        </Form.Group>

                        <Form.Group controlId="lowerLimit" className="mb-3">
                          <Form.Label>임계가격 (하한가)</Form.Label>
                          <Form.Control
                              type="number"
                              placeholder="예: 50000"
                              name="lowerLimit"
                              value={stockData.lowerLimit}
                              onChange={handleChange}
                          />
                        </Form.Group>

                        <Button variant="primary" type="submit" disabled={isSaveDisabled}>
                          {isSaveDisabled ? "비활성화" : "추가"}
                        </Button>
                      </Form>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>
            </Container>
          </Dropdown.Menu>
        </Dropdown>
      </div>
  );
}

export default FixedPlugin;
