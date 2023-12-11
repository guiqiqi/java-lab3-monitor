from matplotlib import pyplot as plt

if __name__ == '__main__':
    humidities, tempratures = [], []
    with open('result.txt') as handler:
        for line in handler:
            if line.startswith('Humidity'):
                humidities.append(float(line.split(': ')[-1]))
            if line.startswith('Temprature'):
                tempratures.append(float(line.split(': ')[-1]))
    iterations = [index for index in range(len(humidities))]
    plt.plot(iterations, humidities, color='blue', linestyle='solid')
    plt.plot(iterations, tempratures, color='red', linestyle='solid')
    plt.axhline(y=30, color='red', linestyle='dashed')
    plt.axhline(y=60, color='blue', linestyle='dashed')
    plt.xlabel('Time')
    plt.ylabel('Value')
    plt.legend(['Humidity', 'Temprature'])
    plt.show()
