# Table of Contents
- [talentshare](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [분석 설계](#분석-설계)
  - [개발 환경](#개발-환경)
  - [구현](#구현)
    - [DDD 의 적용](#ddd의-적용)
    - [Polyglot Persistence](#Polyglot-Persistence)
    - [CQRS](#CQRS)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [Deploy](#Deploy)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [HPA](#HPA)
    - [Readiness](#Readiness)
    - [Liveness](#Liveness)
    - [Persistence Volume](#Persistence-Volume)


다음 평가 항목을 바탕으로 실습을 진행했다.

https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# 서비스 시나리오
회계, 영상 편집, 스포츠 등 다양한 재능을 사용자가 배우거나 도움을 받을 수 있도록 재능을 연결하는 시스템인 talentshare의 요구사항은 다음과 같습니다.

기능적 요구사항

1. 사용자는 원하는 재능을 예약한다.
2. 사용자가 결제를 완료하면 예약이 완료된다.
3. 사용자가 결제를 완료하지 못하면 예약이 취소된다.
4. 예약이 완료되면 담당자는 예약 내역을 확인하고 확정한다.
5. 사용자는 예약 현황을 조회할 수 있다.
6. 사용자는 중간에 예약을 취소할 수 있으며, 해당 예약 및 결제는 취소된다.

비기능적 요구사항

1. 담당자 부재 등으로 예약 내역을 확정할 수 없더라도 사용자는 중단 없이 재능을 예약할 수 있다. (Event Pub/Sub)
2. 결제가 완료되어야 예약이 완료된다. (Req/Res)
3. 부하 발생 시 자동으로 처리 프로세스를 증가시킨다. (Autoscale)
4. 배포 중에 Microservice를 실행해 오류가 발생하지 않도록 조치한다. (Readiness)
5. Microservice의 지속적인 Health check를 통해 이상 발생 시 정해진 회수에 대해 Retry 하고, 동일한 증상이 이어지면 Microservice를 정지한다. (Liveness)


# 분석 설계
## Event Storming

- MSAEZ에서 Event Storming 수행 (http://www.msaez.io/#/storming/He5WER68NAWpdvty4ZEZ4V7rofn1/f7596c11853d6fcd20711e680dab94ba)
- Event 도출

![Event](https://user-images.githubusercontent.com/3106233/130342853-16069fb9-6d96-402f-880e-fb11dd3ce1ed.png)

- Command, Policy 부착

![Event 2](https://user-images.githubusercontent.com/3106233/130342862-9d690cdc-4ea2-4bf7-9a30-005e4f9a4674.png)

- Aggregate 부착

![Event 3](https://user-images.githubusercontent.com/3106233/130342870-30872b28-6a30-4cbe-b283-6d8bb64232f1.png)

- View, Pub/Sub, Req/Res 추가 및 Bounded Context 묶기

![Event 4](https://user-images.githubusercontent.com/3106233/130342873-a61dfacb-3fbd-473a-9ade-ab799c586d5b.png)

기능적 요구사항 커버 여부 검증

1. 사용자는 원하는 숙소를 예약한다. (O)
2. 사용자가 결제를 완료하면 예약이 완료된다. (O)
3. 사용자가 결제를 완료하지 못하면 예약이 취소된다. (O)
4. 예약이 완료되면 담당자는 내역을 확인하고 확정한다. (O)
5. 사용자는 예약 현황을 조회할 수 있다. (O)
6. 사용자는 중간에 예약을 취소할 수 있으며, 해당 예약은 삭제된다. (O)

비기능적 요구사항 커버 여부 검증

1. 담당자 부재 등으로 예약 내역을 확정할 수 없더라도 사용자는 중단 없이 숙소를 예약할 수 있다. (Event Pub/Sub) (O)
2. 결제가 완료되어야 예약이 완료된다. (Req/Res) (O)
3. 부하 발생 시 자동으로 처리 프로세스를 증가시킨다. (Autoscale) (O)

요구사항에 맞도록 Flow가 구성되었는지 확인했다.

![Flow](https://user-images.githubusercontent.com/3106233/131766223-90d209bf-c391-488a-b71b-d507c97ed588.png)

## Hexagonal Architecture Diagram

![슬라이드1](https://user-images.githubusercontent.com/3106233/130350271-d212469e-01b9-4058-873e-8ead538107b9.PNG)

- Inbound adaptor와 Outbound adaptor를 구분함
- 호출관계에서 PubSub 과 Req/Resp 를 구분함
- 서브 도메인과 바운디드 컨텍스트를 분리함


# 개발 환경
Windows Local에 Ubuntu를 설치해 개발을 수행했다.

- Windows WL2 설정
- Ubuntu 설치
- Java: OpenJDK 설치
- Kafka 설치
```
wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz
tar xvf kafka_2.11-2.1.0.tgz
```

- Maven 설치
```
sudo apt install maven
```

- Httpie 설치
```
sudo apt install httpie
```

- eksctl 설치
```
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

- Docker 설치
```
sudo apt-get update
sudo apt install apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"
sudo apt update
sudo apt install docker-ce
sudo usermod -aG docker [Username]
sudo add-apt-repository \
"deb [arch=amd64] https://download.docker.com/linux/ubuntu \
$(lsb_release -cs) \
stable"
sudo systemctl enable docker
sudo service docker start
```

- Helm 설치
- Kubectl 설치
```
sudo apt-get update && sudo apt-get install -y apt-transport-https
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubectl
```

- AWS Client 설치
```
sudo apt-get install unzip
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

- Siege 설치
```
sudo apt install siege
```

- git 설치
```
sudo apt-get install git
sudo apt install git
git --version

git config --global user.name [이름]
git config --global user.mail [메일 주소]
```

- Istio + kiali설치 
```
curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.7.1 TARGET_ARCH=x86_64 sh -
cd istio-1.7.1
export PATH=$PWD/bin:$PATH
istioctl install --set profile=demo --set hub=gcr.io/istio-release
```


# 구현
4개의 Microservice를 Springboot로 구현했으며, 다음과 같이 실행해 Local test를 진행했다. Port number는 8081~8084이다.

```
cd /home/jacesky/code/talentshare/confirmation
mvn spring-boot:run

cd /home/jacesky/code/talentshare/order
mvn spring-boot:run

cd /home/jacesky/code/talentshare/payment
spring-boot:run

/home/jacesky/code/talentshare/retrieve
mvn spring-boot:run
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. 

```
package talentshare;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="PaymentHist_table")
public class PaymentHist {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Long cardNo;
    private String status;

    @PostPersist
    public void onPostPersist(){
        PaymentApproved paymentApproved = new PaymentApproved();
        paymentApproved.setStatus("Payment Completed");
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();

    }
    @PostUpdate
    public void onPostUpdate(){
        PaymentCanceled paymentCanceled = new PaymentCanceled();
        BeanUtils.copyProperties(this, paymentCanceled);
        paymentCanceled.publishAfterCommit();

    }
    @PrePersist
    public void onPrePersist(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getCardNo() {
        return cardNo;
    }

    public void setCardNo(Long cardNo) {
        this.cardNo = cardNo;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package talentshare;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="paymentHist", path="paymentHist")
public interface PaymentHistRepository extends PagingAndSortingRepository<PaymentHist, Long>{
}
```

- 적용 후 REST API 의 테스트
```
# order 서비스의 주문처리
C:\Users\ijm92>http POST http://localhost:8088/orders name=jaehong cardNo=7 status=Ordered talentCategory=Bowling
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:23:57 GMT
Location: http://localhost:8081/orders/1
transfer-encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/1"
        },
        "self": {
            "href": "http://localhost:8081/orders/1"
        }
    },
    "cardNo": 7,
    "name": "jaehong",
    "status": "Ordered",
    "talentCategory": "Bowling"
}

# payment 서비스의 결제처리
C:\Users\ijm92>http GET http://localhost:8088/paymentHist/1
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:24:45 GMT
transfer-encoding: chunked

{
    "_links": {
        "paymentHist": {
            "href": "http://localhost:8082/paymentHist/1"
        },
        "self": {
            "href": "http://localhost:8082/paymentHist/1"
        }
    },
    "cardNo": 7,
    "orderId": 1,
    "status": "Order Submitted"
}

# Confirmation 서비스의 예약처리
C:\Users\ijm92>http GET http://localhost:8088/confirmations/1
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:25:15 GMT
transfer-encoding: chunked

{
    "_links": {
        "confirmation": {
            "href": "http://localhost:8083/confirmations/1"
        },
        "self": {
            "href": "http://localhost:8083/confirmations/1"
        }
    },
    "orderId": 1,
    "status": "Confirmation Complete",
    "talentCategory": null
}
```


## Polyglot Persistence

Polyglot Persistence를 위해 h2datase를 hsqldb로 변경한다.
```
		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<scope>runtime</scope>
		</dependency>
<!--
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
-->
```

src/main/resources 디렉토리에 schema.sql 파일을 생성하고, 필요한 Table을 정의한다. hsqldb가 기동할 때 해당 Table을 생성해 준다.
```
confirmation > schema.sql

DROP TABLE CONFIRMATION IF EXISTS;
CREATE TABLE CONFIRMATION (
orderid BIGINT IDENTITY NOT NULL PRIMARY KEY,
name VARCHAR(20),
status VARCHAR(50)
);
```

PolicyHandler가 Event를 수신할 때 CONFIRMATION Table에 기본 정보를 입력하고, 입력된 결과를 출력하도록 구현한다.
```
confirmation > PolicyHanler.java

public class PolicyHandler{    
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired CancellationRepository cancellationRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_AcceptConfirm(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;
        Confirmation confirmation = new Confirmation();
        confirmation.setStatus("Confirmation Complete");
        confirmation.setOrderId(paymentApproved.getOrderId());
        // confirmation.setTalentCategory(paymentApproved.get);
        confirmation.setId(paymentApproved.getOrderId());
        confirmationRepository.save(confirmation);

        this.jdbc.update("insert into CONFIRMATION (orderid, name, status) values (?, ?, ?)", paymentApproved.getOrderId(), "Chris Choi", "Confirmation Complete");
        List<Map<String, Object>> list = this.jdbc.queryForList("SELECT * FROM CONFIRMATION");
        list.forEach(System.out::println);
    }
```

![hsqldb](https://user-images.githubusercontent.com/3106233/130481775-6f9bfc45-2b8f-4f4c-a709-781c2ba03215.png)


## CQRS

CQRS 구현을 위해 고객의 예약 상황을 확인할 수 있는 Mypage를 구성.

```
# Mypage 확인    
C:\Users\ijm92>http GET http://localhost:8088/mypages/1
HTTP/1.1 200 OK
Content-Type: application/hal+json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:26:00 GMT
transfer-encoding: chunked

{
    "_links": {
        "mypage": {
            "href": "http://localhost:8084/mypages/1"
        },
        "self": {
            "href": "http://localhost:8084/mypages/1"
        }
    },
    "cancellationId": null,
    "confirmationId": 1,
    "name": "jaehong",
    "orderId": 1,
    "status": "Confirmation Complete",
    "talentCategory": "Bowling"
}
```


## 동기식 호출 과 Fallback 처리

주문(Order)->결제(Payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
#PaymentHistoryService.java

package yanolza.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="payment", url="${api.payment.url}")
public interface PaymentHistoryService {
    @RequestMapping(method= RequestMethod.POST, path="/paymentHistories")
    public void pay(@RequestBody PaymentHistory paymentHistory);

}
```

- 주문을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
# Order.java

    @PostPersist
    public void onPostPersist(){
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();

        talentshare.external.PaymentHist paymentHist = new talentshare.external.PaymentHist();
        paymentHist.setOrderId(this.id);
        paymentHist.setStatus("Order Submitted");
        paymentHist.setCardNo(this.cardNo);              
        
        OrderApplication.applicationContext.getBean(talentshare.external.PaymentHistService.class)
            .pay(paymentHist);

    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 결제 (payment) 서비스를 잠시 내려놓음 (ctrl+c)

# 주문요청
C:\Users\ijm92>http POST http://localhost:8088/orders name=jaehong cardNo=8 status=Ordered talentCategory=Bowling
HTTP/1.1 500 Internal Server Error
Content-Type: application/json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:29:10 GMT
transfer-encoding: chunked

{
    "error": "Internal Server Error",
    "message": "Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Error while committing the transaction",
    "path": "/orders",
    "status": 500,
    "timestamp": "2021-08-22T09:29:10.794+0000"
}

# 결제 (payment) 재기동
mvn spring-boot:run

#주문처리
C:\Users\ijm92>http POST http://localhost:8088/orders name=jaehong cardNo=8 status=Ordered talentCategory=Bowling
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:30:19 GMT
Location: http://localhost:8081/orders/6
transfer-encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/6"
        },
        "self": {
            "href": "http://localhost:8081/orders/6"
        }
    },
    "cardNo": 8,
    "name": "jaehong",
    "status": "Ordered",
    "talentCategory": "Bowling"
}
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 예약 시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 예약 시스템의 처리를 위하여 결제주문이 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
confirmation > Cancellation.java
 ...
    @PostPersist
    public void onPostPersist(){
        ConfirmCanceled confirmCanceled = new ConfirmCanceled();
        BeanUtils.copyProperties(this, confirmCanceled);
        confirmCanceled.publishAfterCommit();
    }
```
- 예약 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
confirmation > PolicyHandler.java

@Service
public class PolicyHandler{
    @Autowired ConfirmationRepository confirmationRepository;
    @Autowired CancellationRepository cancellationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_AcceptConfirm(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("\n\n##### listener AcceptConfirm : " + paymentApproved.toJson() + "\n\n");


        Confirmation confirmation = new Confirmation();
        confirmation.setStatus("Confirmation Complete");
        confirmation.setOrderId(paymentApproved.getOrderId());
        // confirmation.setTalentCategory(paymentApproved.get);
        confirmation.setId(paymentApproved.getOrderId());
        confirmationRepository.save(confirmation);

    }
```

예약 시스템은 주문/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 예약시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# Confirmation을 잠시 내려놓음 (ctrl+c)

#주문처리
C:\Users\ijm92>http POST http://localhost:8088/orders name=jaehong cardNo=9 status=Ordered talentCategory=Bowling
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Sun, 22 Aug 2021 09:35:10 GMT
Location: http://localhost:8081/orders/7
transfer-encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/7"
        },
        "self": {
            "href": "http://localhost:8081/orders/7"
        }
    },
    "cardNo": 9,
    "name": "jaehong",
    "status": "Ordered",
    "talentCategory": "Bowling"
}	    
```


# 운영


## Deploy

Local에서 CLI (Command Line Interface) 로 AWS 서비스를 사용하기 위해 AWS 자격 증명을 등록한다.

```
Console > 상단 우측 사용자 클릭 > 내 보안 자격 증명
- 액세스 키 만들기 클릭
- ID, PW를 아래 명령어에 입력

aws configure
- AWS Access Key ID [None]: 위 ID 입력
- AWS Secret Access Key [None]: 위 PW 입력
- Default region name [None]: ap-northeast-2 입력
- Default output format [None]: json 입력

aws configure list
 - 위 설정 등록 여부 확인
```

EKS (Elastic Kubernetes Service) 를 생성한다. 실습 때는 Kubernetes version을 1.17로 설정했으나, Depreciate 된 상태로 1.19로 변경했다.
```
eksctl create cluster --name [EKS name] --version 1.19 --nodegroup-name standard-workers --node-type t3.medium --nodes 4 --nodes-min 1 --nodes-max 4
::EKS name 입력 (지정)
```

AWS Console > EKS
- 클러스터 클릭
- 클러스터에 상태가 활성이면 완료.

![EKS](https://user-images.githubusercontent.com/3106233/131766700-6f4ce83b-2894-4401-b1f8-33d61d71e251.png)

[클러스터 토큰 가져오기. Context arn 추가]
```
aws eks --region ap-northeast-2 update-kubeconfig --name [EKS name]
::Region code, EKS name 입력

kubectl get all
kubectl config current-context
```

Image를 업로드할 ECR을 생성한다. Microservice 별로 하나씩 생성하며, Repository 별 URI를 복사한다. 이어서 ECR 인증 로그인을 수행한다.
```
aws ecr create-repository --repository-name jaehong-retrieve --region ap-northeast-2
aws ecr create-repository --repository-name jaehong-gateway --region ap-northeast-2
aws ecr create-repository --repository-name jaehong-reservation --region ap-northeast-2
aws ecr create-repository --repository-name jaehong-order --region ap-northeast-2
aws ecr create-repository --repository-name jaehong-payment --region ap-northeast-2
::ECR Name, Region

![ECR](https://user-images.githubusercontent.com/3106233/131766887-97e2931e-4941-4262-9611-7f8e001ec73d.png)

docker login --username AWS -p $(aws ecr get-login-password-stdin --region ap-northeast-2) [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/
::Region id, ECR 저장소 입력
```

EKS에 Heml, Kafka 등 필요한 Tool들을 설치한다.
```
[Metric Server 설치]
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.5.0/components.yaml
kubectl get pods -n kube-system -l k8s-app=metrics-server

[Helm 설치]
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh

[EKS에 Kafka 설치]
kubectl --namespace kube-system create sa tiller
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

helm repo add incubator https://charts.helm.sh/incubator
helm repo update
kubectl create ns kafka
helm install my-kafka --namespace kafka incubator/kafka
watch kubectl get all -n kafka
::kafka Pod 3개, Zookeeper 3개 생성

::Siege 설치
kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
```

Code를 GitHub에서 Local로 Clone 한다.
```
git clone https://github.com/chrischoi12/talentshare.git
```

각 Microservice의 pom.xml이 위치한 디렉토리에서 Maven Build를 수행한다
```
cd /home/jacesky/code/talentshare/retrieve
mvn package -Dmaven.test.skip=true

cd /home/jacesky/code/talentshare/gateway
mvn package -Dmaven.test.skip=true

cd /home/jacesky/code/talentshare/order
mvn package -Dmaven.test.skip=true

cd /home/jacesky/code/talentshare/payment
mvn package -Dmaven.test.skip=true

cd /home/jacesky/code/talentshare/confirmation
mvn package -Dmaven.test.skip=true
```

Docker Build 및 Push를 실행한다.
```
cd /home/jacesky/code/talentshare/retrieve
docker build -t [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-retrieve:v1 .
::URI, Image name


cd /home/jacesky/code/talentshare/gateway
docker build -t [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-gateway:v1 .

cd /home/jacesky/code/talentshare/order
docker build -t [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-order:v1 .

cd /home/jacesky/code/talentshare/payment
docker build -t [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-payment:v1 .

cd /home/jacesky/code/talentshare/confirmation
docker build -t [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-confirmation:v1 .

::Image Push
docker push [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-retrieve:v1
docker push [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-gateway:v1
docker push [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-order:v1
docker push [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-payment:v1
docker push [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-confirmation:v1
::URI, Image name

:: 에러 발생하면 도커 수행 후 상태 확인
sudo /etc/init.d/docker start or sudo service docker start or /etc/init.d/docker start
```

Microservice를 Deploy 한다. Service를 Expose 할 때는 Gateway만 LoadBAlancer로 설정한다.
```
kubectl create deploy retrieve --image=[AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-retrieve:v1
kubectl create deploy gateway --image=[AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-gateway:v1
kubectl create deploy order --image=[AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-order:v1
kubectl create deploy payment --image=[AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-payment:v1
kubectl create deploy confirmation --image=[AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-confirmation:v1
::ECR URI, ECR Name

kubectl expose deploy retrieve --type=ClusterIP --port=8080
kubectl expose deploy order --type=ClusterIP --port=8080
kubectl expose deploy payment --type=ClusterIP --port=8080
kubectl expose deploy confirmation --type=ClusterIP --port=8080

kubectl expose deploy gateway --type=LoadBalancer --port=8080
::Gateway만 LoadBalancer로 설정
```

![LoadBalancer](https://user-images.githubusercontent.com/3106233/130342569-7b24f66c-c968-48c4-a317-4250f0c74018.png)


## 동기식 호출 / 서킷 브레이킹 / 장애격리


## Circuit Breaker

Istio를 활용해 Circuit Breaker를 구현했다. Order로 유입되는 요청 건수가 임계치를 초과하게 되면 Circuit Breaker를 이용해 장애 격리 (Fault Isolation) 하도록 한다.

- Istio 설정을 한다.
```
kubectl create namespace istio-system
kubectl get ns -L istio-injection
kubectl label namespace default istio-injection=enabled
kubectl get ns -L istio-injection
```

- yaml 파일에 설정된 DestinationRule에 요청 건수 임계치를 설정한다.
```
kubectl apply -f /home/jacesky/code/talentshare/kubernetes/destination-rule.yaml

apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: order
spec:
  host: order
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 1
        maxRequestsPerConnection: 1
```

- Siege를 사용해 부하를 발생한다. 1명 처리 시 문제 없이 실행되었다.
```
#1명 처리
siege -c1 -t10S -v --content-type "application/json" 'http://order:8080/orders POST {"name": "VIP", "cardNo": "999"}'
```

![circuit 1](https://user-images.githubusercontent.com/3106233/130348891-56d84300-d947-4d2b-8f6e-cf26bdbccfab.png)

- 5명 처리 시 중간 중간 임계치 초과로 인해 에러가 발생했고, Availability는 72% 수준으로 떨어졌다.
```
#5명 처리
siege -c5 -t10S -v --content-type "application/json" 'http://order:8080/orders POST {"name": "VIP", "cardNo": "999"}'
```

![circuit 2](https://user-images.githubusercontent.com/3106233/130348900-99d2b7ca-0e14-423b-bea9-7c363cd6c77b.png)

부하나 상대 시스템의 Req 처리 오류로 인해 Microservice가 죽지 않도록 Circuit Breaker가 미연에 방지한다.


### HPA
Mypage 조회 건수 증가 시 Pod를 동적으로 증가시키도록 HPA (Auto-scaleout) 을 실행한다.

- yaml 파일에 Mypage Pod 실행 시 resources 설정을 추가한다.
```
kubectl apply -f /home/jacesky/code/talentshare/kubernetes/autoscaleout_retrieve.yaml

          resources:
            requests:
              cpu: "200m"
            limits:
              cpu: "500m"
```

- autoscale 설정 시 CPU resource의 50%를 임계치로 두고, 임계치를 넘게 되면 최대 5개까지 Pod가 증가하도록 한다.
```
kubectl autoscale deployment retrieve --cpu-percent=50 --min=1 --max=5
```

- Siege를 활용해 부하를 준다.
```
siege -c255 -t60S -v --content-type "application/json" 'http://retrieve:8080/'
```

- Pod가 점차 증가하는 것을 확인할 수 있다.

![hap 1](https://user-images.githubusercontent.com/3106233/130348172-cb1e3fb9-0479-41a9-b870-e650cb11a1a1.png)

![hpa 2](https://user-images.githubusercontent.com/3106233/130348373-c18a63c2-357a-4c7b-be9b-c8ddfcd1920b.png)


## Readiness
- 배포를 위해 retrieve microservice를 기존 v1 외에 추가적으로 v2를 Docker push 한다.
```
cd /home/jacesky/code/talentshare/retrieve
mvn package -Dmaven.test.skip=true
docker build -t [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-retrieve:v2 .
docker push [AWS 12자리 계정].dkr.ecr.ap-northeast-2.amazonaws.com/jaehong-retrieve:v2
```

Readiness 설정이 없는 yml 파일로 v1 이미지를 Deploy 한다.
```
kubectl apply -f /home/jacesky/yanolza-team/kubernetes/deployment_readiness_v1.yml
```

Siege에 접속해 retrieve microservice를 실행한다.
```
kubectl exec pod/siege-c54d6bdc7-k9mj5 -it -- /bin/bash
siege -v -c1 -t80S http://retrieve:8080
```

Readiness 설정이 없는 yml 파일로 v2 이미지를 Deploy 한다.
```
kubectl apply -f /home/jacesky/code/talentshare/kubernetes/deployment_readiness_v2.yml
```

Availability가 50%대로 떨어졌음을 확인했다. Readiness가 보장되지 않아 배포 중 서비스에 접근할 수 없어서 에러가 발생한 것이다.

![readiness_1](https://user-images.githubusercontent.com/3106233/130343426-2e6baed3-e582-42fb-8a12-6ca8f8c794af.png)

v1 이미지로 원복한다.
```
kubectl apply -f /home/jacesky/yanolza-team/kubernetes/deployment_readiness_v1.yml
```

Siege에 접속해 retrieve microservice를 실행한다.
```
kubectl exec pod/siege-c54d6bdc7-k9mj5 -it -- /bin/bash
siege -v -c1 -t80S http://retrieve:8080
```

Readiness 설정이 포함된 yml로 v2 이미지를 Deploy 한다.
```
kubectl apply -f /home/jacesky/code/talentshare/kubernetes/deployment_readiness_v3.yml

          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```

Availability가 99%대로 증가했다.

![readiness_2](https://user-images.githubusercontent.com/3106233/130343501-91db1b5c-f821-4237-a4ba-a355c1ab16e3.png)


## Liveness

yml 내에 임의로 Health check 대상이 되는 디렉토리를 삭제(rm) 하도록 설정한다. livenessProbe 설정에서 해당 디렉토리를 체크하도록 되어 있는데, 정상적이지 않은 상태로 판단하고 Retry를 시도하다가, 임계치 회수가 초과하면 서비스를 중지한다.
```
kubectl apply -f /home/jacesky/code/talentshare/kubernetes/deployment_liveness.yml

          args:
          - /bin/sh
          - -c
          - touch /tmp/healthy; sleep 90; rm -rf /tmp/healthy; sleep 600
          ports:
            - containerPort: 8080
          livenessProbe:
            exec:
              command:
              - cat
              - /tmp/healthy
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

RESTARTS 회수가 증가하는 것을 확인했다.

![live 1](https://user-images.githubusercontent.com/3106233/130343737-4a129246-be03-4309-a840-447c03833ca4.png)

failureThreshold를 넘어서면 CrashLoopBackOff 상태로 서비스가 중지된다.

![live 2](https://user-images.githubusercontent.com/3106233/130343747-7f942b6d-f4fe-423d-8036-75a1d5d6e7c3.png)


## Persistence VolumeE

EFS (Elastic File Storage) 를 신규로 생성한 후, Pod가 EFS에 접근할 수 있또록 설정한다.

- EFS 생성: ClusterSharedNodeSecurityGroup 선택
- EFS계정 생성 및 Role 바인딩
```
- ServerAccount 생성
kubectl apply -f efs-sa.yml
kubectl get ServiceAccount efs-provisioner -n yanolza

-SA(efs-provisioner)에 권한(rbac) 설정
kubectl apply -f efs-rbac.yaml

# efs-provisioner-deploy.yml 파일 수정
value: fs-3ddc505d
value: ap-northeast-2
server: fs-3ddc505d.efs.ap-northeast-2.amazonaws.com
```

- EFS provisioner 설치
```
kubectl apply -f efs-provisioner-deploy.yml
kubectl get Deployment efs-provisioner -n yanolza
```

- EFS storageclass 생성
```
kubectl apply -f efs-storageclass.yaml
kubectl get sc aws-efs -n yanolza
```

- PVC 생성
```
kubectl apply -f volume-pvc.yml
kubectl get pvc -n yanolza
```

- Create Pod with PersistentVolumeClaim
```
kubectl apply -f pod-with-pvc.yaml
```

- df-k로 EFS에 접근 가능

![dfk](https://user-images.githubusercontent.com/3106233/130389161-454819e2-4353-479a-958d-7ca2ad45a731.png)
